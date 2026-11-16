package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.PlanResponse;
import cn.resumecraft.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final ObjectMapper objectMapper;

  @GetMapping
  ApiResponse<List<PlanResponse>> list() {
    return ApiResponse.ok(
        orderService.plans().stream().map(plan -> PlanResponse.from(plan, objectMapper)).toList());
  }
}
