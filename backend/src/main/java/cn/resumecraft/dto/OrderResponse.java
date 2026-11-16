package cn.resumecraft.dto;

import cn.resumecraft.entity.Order;
import java.time.LocalDateTime;

public record OrderResponse(
    Long id,
    String orderNo,
    Long planId,
    String planName,
    Integer durationDays,
    Integer amount,
    String status,
    LocalDateTime createdAt,
    LocalDateTime paidAt,
    LocalDateTime expiredAt) {
  public static OrderResponse from(Order order) {
    return new OrderResponse(
        order.id,
        order.orderNo,
        order.planId,
        order.planName,
        order.durationDays,
        order.amount,
        order.status,
        order.createdAt,
        order.paidAt,
        order.expiredAt);
  }
}
