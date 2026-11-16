package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.CreateOrderRequest;
import cn.resumecraft.entity.Order;
import cn.resumecraft.security.LoginUser;
import cn.resumecraft.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
  ApiResponse<Order> create(
      @AuthenticationPrincipal LoginUser loginUser,
      @Valid @RequestBody CreateOrderRequest request) {
    return ApiResponse.ok(
        orderService.create(loginUser.userId(), request.planId(), request.requestKey()));
  }

  @GetMapping
  ApiResponse<List<Order>> list(@AuthenticationPrincipal LoginUser loginUser) {
    return ApiResponse.ok(orderService.list(loginUser.userId()));
  }

  @GetMapping("/{id}")
  ApiResponse<Order> detail(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
    return ApiResponse.ok(orderService.detail(loginUser.userId(), id));
  }
}
