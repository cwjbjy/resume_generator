package cn.resumecraft.service;

import static cn.resumecraft.api.BizException.require;

import cn.resumecraft.dto.PaymentCallback;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentSignatureService {
  private final String secret;

  public PaymentSignatureService(@Value("${app.payment-secret}") String secret) {
    if (secret.isBlank()) {
      throw new IllegalStateException("PAYMENT_SECRET must be configured");
    }
    this.secret = secret;
  }

  public void verify(PaymentCallback callback, String signature) {
    require(
        callback.orderNo() != null
            && callback.amount() != null
            && callback.transactionId() != null
            && callback.transactionId().matches("[A-Za-z0-9_-]{1,100}")
            && Set.of("SUCCESS", "FAILED")
                .contains(callback.status() == null ? "" : callback.status()),
        400,
        "回调格式错误");
    require(Math.abs(Instant.now().getEpochSecond() - callback.timestamp()) <= 300, 400, "回调时间已过期");
    require(
        signature != null
            && MessageDigest.isEqual(
                sign(callback).getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)),
        400,
        "支付签名无效");
  }

  public String sign(PaymentCallback callback) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of()
          .formatHex(mac.doFinal(canonical(callback).getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to sign payment callback", exception);
    }
  }

  private String canonical(PaymentCallback callback) {
    return callback.orderNo()
        + "\n"
        + callback.amount()
        + "\n"
        + callback.transactionId()
        + "\n"
        + callback.status()
        + "\n"
        + callback.timestamp();
  }
}
