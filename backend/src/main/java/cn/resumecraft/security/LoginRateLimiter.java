package cn.resumecraft.security;

import cn.resumecraft.exception.BizException;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class LoginRateLimiter {
  private static final DefaultRedisScript<Long> ACQUIRE =
      new DefaultRedisScript<>(
          """
          local retry = 0
          for i = 1, 2 do
            local count = tonumber(redis.call('GET', KEYS[i]) or '0')
            if count >= tonumber(ARGV[(i - 1) * 2 + 1]) then
              retry = math.max(retry, redis.call('PTTL', KEYS[i]), 1)
            end
          end
          if retry > 0 then return retry end
          for i = 1, 2 do
            local count = redis.call('INCR', KEYS[i])
            if count == 1 then
              redis.call('PEXPIRE', KEYS[i], ARGV[i * 2])
            end
          end
          return 0
          """,
          Long.class);

  private final StringRedisTemplate redis;
  private final int ipMaxAttempts;
  private final long ipWindowMillis;
  private final int accountMaxAttempts;
  private final long accountWindowMillis;

  public LoginRateLimiter(
      StringRedisTemplate redis,
      @Value("${app.security.login-limit.ip-max-attempts:60}") int ipMaxAttempts,
      @Value("${app.security.login-limit.ip-window-seconds:60}") long ipWindowSeconds,
      @Value("${app.security.login-limit.account-max-attempts:10}") int accountMaxAttempts,
      @Value("${app.security.login-limit.account-window-seconds:300}") long accountWindowSeconds) {
    if (ipMaxAttempts < 1
        || accountMaxAttempts < 1
        || ipWindowSeconds < 1
        || accountWindowSeconds < 1) {
      throw new IllegalArgumentException("登录限流阈值和窗口必须大于零");
    }
    this.redis = redis;
    this.ipMaxAttempts = ipMaxAttempts;
    this.ipWindowMillis = Math.multiplyExact(ipWindowSeconds, 1000);
    this.accountMaxAttempts = accountMaxAttempts;
    this.accountWindowMillis = Math.multiplyExact(accountWindowSeconds, 1000);
  }

  public void acquire(String remoteAddress, String username) {
    try {
      Long retryMillis =
          redis.execute(
              ACQUIRE,
              List.of("rc:auth:login:ip:" + remoteAddress, accountKey(username)),
              Integer.toString(ipMaxAttempts),
              Long.toString(ipWindowMillis),
              Integer.toString(accountMaxAttempts),
              Long.toString(accountWindowMillis));
      if (retryMillis == null) throw new BizException(503, "登录服务暂时不可用，请稍后重试");
      if (retryMillis > 0) throw new LoginRateLimitException((retryMillis + 999) / 1000);
    } catch (DataAccessException exception) {
      throw new BizException(503, "登录服务暂时不可用，请稍后重试");
    }
  }

  static String accountKey(String username) {
    return "rc:auth:login:account:" + username.toLowerCase(Locale.ROOT);
  }
}
