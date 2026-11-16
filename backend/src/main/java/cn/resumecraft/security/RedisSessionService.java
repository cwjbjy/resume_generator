package cn.resumecraft.security;

import cn.resumecraft.exception.BizException;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSessionService {
  private static final DefaultRedisScript<Long> REPLACE =
      new DefaultRedisScript<>(
          """
          redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
          redis.call('DEL', KEYS[2])
          return 1
          """,
          Long.class);
  private static final DefaultRedisScript<Long> REVOKE =
      new DefaultRedisScript<>(
          """
          if redis.call('GET', KEYS[1]) == ARGV[1] then
            return redis.call('DEL', KEYS[1])
          end
          return 0
          """,
          Long.class);
  private final StringRedisTemplate redis;

  public void replace(Long userId, String username, String sessionId, Duration lifetime) {
    Long result =
        accessRedis(
            () ->
                redis.execute(
                    REPLACE,
                    List.of(sessionKey(userId), LoginRateLimiter.accountKey(username)),
                    sessionId,
                    Long.toString(lifetime.toMillis())));
    if (result == null) throw unavailable();
  }

  public boolean isCurrent(Long userId, String sessionId) {
    return sessionId.equals(accessRedis(() -> redis.opsForValue().get(sessionKey(userId))));
  }

  public void revoke(Long userId, String sessionId) {
    Long result = accessRedis(() -> redis.execute(REVOKE, List.of(sessionKey(userId)), sessionId));
    if (result == null) throw unavailable();
  }

  private String sessionKey(Long userId) {
    return "rc:auth:session:" + userId;
  }

  private <T> T accessRedis(Supplier<T> operation) {
    try {
      return operation.get();
    } catch (DataAccessException exception) {
      throw unavailable();
    }
  }

  private BizException unavailable() {
    return new BizException(503, "登录服务暂时不可用，请稍后重试");
  }
}
