package cn.resumecraft.service;

import static cn.resumecraft.exception.BizException.require;

import cn.resumecraft.dto.PaymentCallback;
import cn.resumecraft.entity.Order;
import cn.resumecraft.entity.User;
import cn.resumecraft.mapper.OrderMapper;
import cn.resumecraft.mapper.PlanMapper;
import cn.resumecraft.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private final OrderMapper orderMapper;
  private final PlanMapper planMapper;
  private final UserMapper userMapper;
  private final OrderService orderService;
  private final PaymentSignatureService signatureService;
  private final PaymentLogService paymentLogService;

  @Transactional
  public String callback(PaymentCallback callback, String signature) {
    signatureService.verify(callback, signature);
    Order found =
        orderMapper.selectOne(
            new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, callback.orderNo()));
    require(found != null, 404, "订单不存在");
    // Every membership mutation acquires the user lock before the order lock.
    User user = userMapper.lock(found.userId);
    Order order = orderMapper.lock(found.id);
    require(order.amount.equals(callback.amount()), 400, "支付金额不匹配");
    if (order.status.equals("PAID")) {
      require(
          callback.status().equals("SUCCESS")
              && Objects.equals(order.transactionId, callback.transactionId()),
          409,
          "交易信息不匹配");
      paymentLogService.record(order, callback, "DUPLICATE");
      return "success";
    }
    require(
        order.status.equals("CREATED") && order.expiredAt.isAfter(LocalDateTime.now()),
        409,
        "订单已关闭或已过期");
    if (callback.status().equals("FAILED")) {
      paymentLogService.record(order, callback, "FAILED");
      return "success";
    }
    markPaid(order, callback.transactionId());
    extendMembership(user, planMapper.selectById(order.planId).durationDays);
    paymentLogService.record(order, callback, "SUCCESS");
    return "success";
  }

  @Transactional
  public Order refund(Long userId, Long orderId) {
    orderService.owned(userId, orderId);
    User user = userMapper.lock(userId);
    Order order = orderMapper.lock(orderId);
    require(order.status.equals("PAID"), 409, "只有已支付订单可以退款");
    orderMapper.update(
        null,
        new LambdaUpdateWrapper<Order>()
            .eq(Order::getId, orderId)
            .eq(Order::getStatus, "PAID")
            .set(Order::getStatus, "REFUNDED"));
    int days = planMapper.selectById(order.planId).durationDays;
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiry = user.memberExpireAt == null ? now : user.memberExpireAt.minusDays(days);
    if (expiry.isBefore(now)) {
      expiry = now;
    }
    userMapper.update(
        null,
        new LambdaUpdateWrapper<User>()
            .eq(User::getId, userId)
            .set(User::getMemberExpireAt, expiry));
    paymentLogService.record(
        order, Map.of("refundOrderNo", order.orderNo, "retractedDays", days), "REFUNDED");
    return orderMapper.selectById(orderId);
  }

  private void markPaid(Order order, String transactionId) {
    int updated =
        orderMapper.update(
            null,
            new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.id)
                .eq(Order::getStatus, "CREATED")
                .set(Order::getStatus, "PAID")
                .set(Order::getPaidAt, LocalDateTime.now())
                .set(Order::getTransactionId, transactionId));
    require(updated == 1, 409, "订单状态已改变");
  }

  private void extendMembership(User user, int days) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime base =
        user.memberExpireAt != null && user.memberExpireAt.isAfter(now) ? user.memberExpireAt : now;
    userMapper.update(
        null,
        new LambdaUpdateWrapper<User>()
            .eq(User::getId, user.id)
            .set(User::getMemberExpireAt, base.plusDays(days)));
  }
}
