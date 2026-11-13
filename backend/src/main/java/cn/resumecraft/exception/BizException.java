package cn.resumecraft.exception;

public class BizException extends RuntimeException {
  public final int status;

  public BizException(int status, String message) {
    super(message);
    this.status = status;
  }

  public static void require(boolean condition, int status, String message) {
    if (!condition) throw new BizException(status, message);
  }
}
