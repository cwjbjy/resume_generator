package cn.resumecraft.service;

import static cn.resumecraft.api.BizException.require;

import cn.resumecraft.mapper.OrderMapper;
import cn.resumecraft.mapper.PlanMapper;
import cn.resumecraft.mapper.UserMapper;
import cn.resumecraft.model.Order;
import cn.resumecraft.model.Plan;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
  private final OrderMapper orderMapper;
  private final PlanMapper planMapper;
  private final UserMapper userMapper;

  public OrderService(OrderMapper orderMapper, PlanMapper planMapper, UserMapper userMapper) {
    this.orderMapper = orderMapper;
    this.planMapper = planMapper;
    this.userMapper = userMapper;
  }

  public List<Plan> plans() {
    return planMapper.selectList(new QueryWrapper<Plan>().orderByAsc("sort"));
  }

  public List<Order> list(Long userId) {
    return orderMapper.selectList(
        new QueryWrapper<Order>().eq("user_id", userId).orderByDesc("created_at"));
  }

  public Order owned(Long userId, Long orderId) {
    Order order = orderMapper.selectById(orderId);
    require(order != null, 404, "订单不存在");
    require(order.userId.equals(userId), 403, "无权访问此订单");
    return order;
  }

  public Order detail(Long userId, Long orderId) {
    Order order = owned(userId, orderId);
    if (order.status.equals("CREATED") && order.expiredAt.isBefore(LocalDateTime.now())) {
      expire();
      return owned(userId, orderId);
    }
    return order;
  }

  @Transactional
  public Order create(Long userId, Long planId, String requestKey) {
    userMapper.lock(userId);
    Order existing =
        orderMapper.selectOne(
            new QueryWrapper<Order>().eq("user_id", userId).eq("request_key", requestKey));
    if (existing != null) {
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
        new UpdateWrapper<Order>()
            .eq("status", "CREATED")
            .lt("expired_at", LocalDateTime.now())
            .set("status", "EXPIRED"));
  }
}
