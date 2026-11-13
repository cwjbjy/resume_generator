package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.entity.Plan;
import cn.resumecraft.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {
  private final OrderService orderService;

  @GetMapping
  ApiResponse<List<Plan>> list() {
    return ApiResponse.ok(orderService.plans());
  }
}
