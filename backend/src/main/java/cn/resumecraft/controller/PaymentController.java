package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.PaymentCallback;
import cn.resumecraft.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService paymentService;

  @PostMapping("/callback")
  ApiResponse<String> callback(
      @RequestBody PaymentCallback callback,
      @RequestHeader(value = "X-Pay-Signature", required = false) String signature) {
    return ApiResponse.ok(paymentService.callback(callback, signature));
  }
}
