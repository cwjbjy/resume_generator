package cn.resumecraft.api;

import cn.resumecraft.dto.MockPaymentRequest;
import cn.resumecraft.dto.PaymentCallback;
import cn.resumecraft.model.Order;
import cn.resumecraft.service.OrderService;
import cn.resumecraft.service.PaymentService;
import cn.resumecraft.service.PaymentSignatureService;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/api")
public class MockPaymentController {
  private final OrderService orderService;
  private final PaymentService paymentService;
  private final PaymentSignatureService signatureService;

  public MockPaymentController(
      OrderService orderService,
      PaymentService paymentService,
      PaymentSignatureService signatureService) {
    this.orderService = orderService;
    this.paymentService = paymentService;
    this.signatureService = signatureService;
  }

  @PostMapping("/pay/mock")
  Api<Order> mock(Authentication authentication, @Valid @RequestBody MockPaymentRequest request) {
    Long userId = (Long) authentication.getPrincipal();
    Order order = orderService.owned(userId, request.orderId());
    PaymentCallback callback =
        new PaymentCallback(
            order.orderNo,
            order.amount,
            "MOCK_" + order.orderNo,
            request.success() ? "SUCCESS" : "FAILED",
            Instant.now().getEpochSecond());
    paymentService.callback(callback, signatureService.sign(callback));
    return Api.ok(orderService.owned(userId, order.id));
  }

  @PostMapping("/orders/{id}/refund")
  Api<Order> refund(Authentication authentication, @PathVariable Long id) {
    return Api.ok(paymentService.refund((Long) authentication.getPrincipal(), id));
  }
}
