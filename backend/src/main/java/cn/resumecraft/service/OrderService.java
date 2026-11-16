package cn.resumecraft.service;

import static cn.resumecraft.exception.BizException.require;

import cn.resumecraft.dto.OrderResponse;
import cn.resumecraft.dto.PageResponse;
import cn.resumecraft.entity.Order;
import cn.resumecraft.entity.Plan;
import cn.resumecraft.mapper.OrderMapper;
import cn.resumecraft.mapper.PlanMapper;
import cn.resumecraft.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final OrderMapper orderMapper;
  private final PlanMapper planMapper;
  private final UserMapper userMapper;

  public List<Plan> plans() {
    return planMapper.selectList(new LambdaQueryWrapper<Plan>().orderByAsc(Plan::getSort));
  }

  public PageResponse<OrderResponse> list(Long userId, int page, int size) {
    var result =
        orderMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt, Order::getId));
    return PageResponse.from(result.convert(OrderResponse::from));
  }

  public Order owned(Long userId, Long orderId) {
    Order order = orderMapper.selectById(orderId);
    require(order != null, 404, "订单不存在");
    require(order.userId.equals(userId), 403, "无权访问此订单");
    return order;
  }

  public Order detail(Long userId, Long orderId) {
    Order order = owned(userId, orderId);
    LocalDateTime now = LocalDateTime.now();
    if (order.status.equals("CREATED") && !order.expiredAt.isAfter(now)) {
      orderMapper.update(
          null,
          new LambdaUpdateWrapper<Order>()
              .eq(Order::getId, orderId)
              .eq(Order::getStatus, "CREATED")
              .le(Order::getExpiredAt, now)
              .set(Order::getStatus, "EXPIRED"));
      return owned(userId, orderId);
    }
    return order;
  }

  @Transactional
  public Order create(Long userId, Long planId, String requestKey) {
    userMapper.lock(userId);
    Order existing =
        orderMapper.selectOne(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getRequestKey, requestKey));
    if (existing != null) {
      require(existing.planId.equals(planId), 409, "幂等请求参数不一致");
      return existing;
    }
    Plan plan = planMapper.selectById(planId);
    require(plan != null, 404, "套餐不存在");
    Order order = new Order();
    String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    order.orderNo = "RC" + date + suffix;
    order.userId = userId;
    order.planId = planId;
    order.planName = plan.name;
    order.durationDays = plan.durationDays;
    order.amount = plan.price;
    order.status = "CREATED";
    order.channel = "MOCK";
    order.requestKey = requestKey;
    order.createdAt = LocalDateTime.now();
    order.expiredAt = order.createdAt.plusMinutes(15);
    orderMapper.insert(order);
    return order;
  }

  @Scheduled(fixedDelay = 60000)
  public void expire() {
    orderMapper.update(
        null,
        new LambdaUpdateWrapper<Order>()
            .eq(Order::getStatus, "CREATED")
            .lt(Order::getExpiredAt, LocalDateTime.now())
            .set(Order::getStatus, "EXPIRED"));
  }
}
