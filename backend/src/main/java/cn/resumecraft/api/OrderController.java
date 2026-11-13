package cn.resumecraft.api;

import cn.resumecraft.dto.CreateOrderRequest;
import cn.resumecraft.model.Order;
import cn.resumecraft.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  @PostMapping
  Api<Order> create(Authentication authentication, @Valid @RequestBody CreateOrderRequest request) {
    return Api.ok(
        orderService.create(
            (Long) authentication.getPrincipal(), request.planId(), request.requestKey()));
  }

  @GetMapping
  Api<List<Order>> list(Authentication authentication) {
    return Api.ok(orderService.list((Long) authentication.getPrincipal()));
  }

  @GetMapping("/{id}")
  Api<Order> detail(Authentication authentication, @PathVariable Long id) {
    return Api.ok(orderService.detail((Long) authentication.getPrincipal(), id));
  }
}
