package cn.resumecraft.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cn.resumecraft.exception.BizException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {
  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void exposesUserIdAndUsernameFromVerifiedToken() throws Exception {
    RedisSessionService sessions = mock(RedisSessionService.class);
    when(sessions.isCurrent(any(), any())).thenReturn(true);
    TokenService tokenService =
        new TokenService("test-jwt-secret-for-login-user-principal", 2, sessions);
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService, new ObjectMapper());
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
    request.addHeader("Authorization", "Bearer " + tokenService.create(42L, "developer01"));
    AtomicBoolean chainInvoked = new AtomicBoolean();

    filter.doFilter(
        request,
        new MockHttpServletResponse(),
        (servletRequest, servletResponse) -> {
          chainInvoked.set(true);
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          assertThat(authentication).isNotNull();
          assertThat(authentication.isAuthenticated()).isTrue();
          assertThat(authentication.getPrincipal())
              .isInstanceOfSatisfying(
                  LoginUser.class,
                  loginUser -> {
                    assertThat(loginUser.userId()).isEqualTo(42L);
                    assertThat(loginUser.username()).isEqualTo("developer01");
                  });
          assertThat(authentication.getName()).isEqualTo("developer01");
        });

    assertThat(chainInvoked).isTrue();
  }

  @Test
  void sessionStoreFailuresReturnReadable503WithoutInvokingTheController() throws Exception {
    TokenService tokens = mock(TokenService.class);
    doThrow(new BizException(503, "登录服务暂时不可用，请稍后重试")).when(tokens).parse("token");
    var request = new MockHttpServletRequest("GET", "/api/users/me");
    request.addHeader("Authorization", "Bearer token");
    var response = new MockHttpServletResponse();
    AtomicBoolean chainInvoked = new AtomicBoolean();
    new JwtAuthenticationFilter(tokens, new ObjectMapper())
        .doFilter(request, response, (req, res) -> chainInvoked.set(true));
    assertThat(response.getStatus()).isEqualTo(503);
    assertThat(response.getContentAsString()).contains("登录服务暂时不可用");
    assertThat(chainInvoked).isFalse();
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
