package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.resumecraft.entity.User;
import cn.resumecraft.exception.BizException;
import cn.resumecraft.security.LoginRateLimitException;
import cn.resumecraft.security.LoginRateLimiter;
import cn.resumecraft.security.RedisSessionService;
import cn.resumecraft.security.TokenService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class LoginSecurityFlowTest extends ApiTestSupport {
  @Autowired StringRedisTemplate redis;
  @Autowired TokenService tokens;
  @MockitoSpyBean RedisSessionService sessions;

  @Test
  void newLoginRevokesOnlyPreviousSessionsForTheSameAccount() throws Exception {
    Account first = account();
    Account other = account();
    User user = users.selectById(first.id());
    var login =
        call(
            "POST",
            "/api/auth/login",
            null,
            Map.of("username", user.username, "password", "test123456"),
            200);
    Account current = new Account(first.id(), login.path("token").asText());
    assertThat(current.token()).isNotEqualTo(first.token());
    call("GET", "/api/users/me", first, null, 401);
    call("GET", "/api/users/me", current, null, 200);
    call("GET", "/api/users/me", other, null, 200);
    call("POST", "/api/auth/logout", first, null, 401);
    tokens.revoke(first.token());
    call("GET", "/api/users/me", current, null, 200);
    call("POST", "/api/auth/logout", current, null, 200);
    call("GET", "/api/users/me", current, null, 401);
    call("GET", "/api/users/me", other, null, 200);
    call("POST", "/api/auth/logout", null, null, 401);
  }

  @Test
  void exhaustedAccountLimitReturnsRetryAfterWithoutCallingAuthentication() throws Exception {
    Account owner = account();
    String username = users.selectById(owner.id()).username;
    redis.opsForValue().set("rc:auth:login:account:" + username, "10", Duration.ofSeconds(30));
    mvc.perform(
            post("/api/auth/login")
                .with(
                    request -> {
                      request.setRemoteAddr("198.51.100.21");
                      return request;
                    })
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsBytes(
                        Map.of("username", username.toUpperCase(), "password", "test123456"))))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"))
        .andExpect(jsonPath("$.code").value(429));
    call("GET", "/api/users/me", owner, null, 200);
  }

  @Test
  void forwardedHeadersCannotBypassTheRemoteAddressLimit() throws Exception {
    String address = "198.51.100.22";
    redis.opsForValue().set("rc:auth:login:ip:" + address, "60", Duration.ofSeconds(30));
    mvc.perform(
            post("/api/auth/login")
                .with(
                    request -> {
                      request.setRemoteAddr(address);
                      return request;
                    })
                .header("X-Forwarded-For", "203.0.113.99")
                .header("X-Real-IP", "203.0.113.99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsBytes(
                        Map.of("username", "missing_user", "password", "test123456"))))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"));
  }

  @Test
  void loginCountersAreAtomicAndSuccessKeepsTheIpCounter() throws Exception {
    String username = "atomic_" + UUID.randomUUID();
    String address = "atomic_" + UUID.randomUUID();
    LoginRateLimiter limiter = new LoginRateLimiter(redis, 7, 60, 100, 60);
    try (var pool = Executors.newFixedThreadPool(4)) {
      List<Callable<Boolean>> attempts = new ArrayList<>();
      for (int index = 0; index < 20; index++) {
        attempts.add(
            () -> {
              try {
                limiter.acquire(address, username);
                return true;
              } catch (LoginRateLimitException rejected) {
                assertThat(rejected.retryAfterSeconds()).isPositive();
                return false;
              }
            });
      }
      long accepted = 0;
      for (var result : pool.invokeAll(attempts)) if (result.get()) accepted++;
      assertThat(accepted).isEqualTo(7);
    }
    sessions.replace(999999L, username, "session_test", Duration.ofMinutes(1));
    assertThat(redis.hasKey("rc:auth:login:account:" + username)).isFalse();
    assertThat(redis.opsForValue().get("rc:auth:login:ip:" + address)).isEqualTo("7");
    assertThat(redis.getExpire("rc:auth:login:ip:" + address)).isPositive();
  }

  @Test
  void failedSessionCreationRollsBackRegistrationAndAllowsRetry() throws Exception {
    String username = "rollback_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    Map<String, String> credentials = Map.of("username", username, "password", "test123456");
    doThrow(new BizException(503, "登录服务暂时不可用，请稍后重试"))
        .when(sessions)
        .replace(any(), any(), any(), any());
    call("POST", "/api/auth/register", null, credentials, 503);
    assertThat(users.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)))
        .isZero();
    reset(sessions);
    call("POST", "/api/auth/register", null, credentials, 200);
  }
}
