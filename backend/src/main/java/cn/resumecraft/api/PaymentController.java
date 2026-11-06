package cn.resumecraft.api;

import cn.resumecraft.dto.PaymentCallback;
import cn.resumecraft.service.PaymentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pay")
public class PaymentController {
  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostMapping("/callback")
  Api<String> callback(
      @RequestBody PaymentCallback callback,
      @RequestHeader(value = "X-Pay-Signature", required = false) String signature) {
    return Api.ok(paymentService.callback(callback, signature));
  }
}
