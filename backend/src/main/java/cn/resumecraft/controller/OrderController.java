package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.CreateOrderRequest;
import cn.resumecraft.dto.OrderResponse;
import cn.resumecraft.dto.PageResponse;
import cn.resumecraft.security.LoginUser;
import cn.resumecraft.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  @PostMapping
  ApiResponse<OrderResponse> create(
      @AuthenticationPrincipal LoginUser loginUser,
      @Valid @RequestBody CreateOrderRequest request) {
    return ApiResponse.ok(
        OrderResponse.from(
            orderService.create(loginUser.userId(), request.planId(), request.requestKey())));
  }

  @GetMapping
  ApiResponse<PageResponse<OrderResponse>> list(
      @AuthenticationPrincipal LoginUser loginUser,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "12") @Min(1) @Max(100) int size) {
    return ApiResponse.ok(orderService.list(loginUser.userId(), page, size));
  }

  @GetMapping("/{id}")
  ApiResponse<OrderResponse> detail(
      @AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
    return ApiResponse.ok(OrderResponse.from(orderService.detail(loginUser.userId(), id)));
  }
}
