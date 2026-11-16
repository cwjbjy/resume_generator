package cn.resumecraft.security;

import cn.resumecraft.exception.BizException;

public class LoginRateLimitException extends BizException {
  private final long retryAfterSeconds;

  public LoginRateLimitException(long retryAfterSeconds) {
    super(429, "登录操作过于频繁，请稍后重试");
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public long retryAfterSeconds() {
    return retryAfterSeconds;
  }
}
