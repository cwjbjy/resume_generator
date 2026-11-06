package cn.resumecraft.api;

import cn.resumecraft.model.Plan;
import cn.resumecraft.service.OrderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
  private final OrderService orderService;

  public PlanController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  Api<List<Plan>> list() {
    return Api.ok(orderService.plans());
  }
}
