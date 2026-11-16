package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.MockPaymentRequest;
import cn.resumecraft.dto.PaymentCallback;
import cn.resumecraft.entity.Order;
import cn.resumecraft.security.LoginUser;
import cn.resumecraft.service.OrderService;
import cn.resumecraft.service.PaymentService;
import cn.resumecraft.service.PaymentSignatureService;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/api")
@RequiredArgsConstructor
public class MockPaymentController {
  private final OrderService orderService;
  private final PaymentService paymentService;
  private final PaymentSignatureService signatureService;

  @PostMapping("/pay/mock")
  ApiResponse<Order> mock(
      @AuthenticationPrincipal LoginUser loginUser,
      @Valid @RequestBody MockPaymentRequest request) {
    Long userId = loginUser.userId();
    Order order = orderService.owned(userId, request.orderId());
    PaymentCallback callback =
        new PaymentCallback(
            order.orderNo,
            order.amount,
            "MOCK_" + order.orderNo,
            request.success() ? "SUCCESS" : "FAILED",
            Instant.now().getEpochSecond());
    paymentService.callback(callback, signatureService.sign(callback));
    return ApiResponse.ok(orderService.owned(userId, order.id));
  }

  @PostMapping("/orders/{id}/refund")
  ApiResponse<Order> refund(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
    return ApiResponse.ok(paymentService.refund(loginUser.userId(), id));
  }
}
