package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("orders")
public class Order {
  @TableId public Long id;
  public String orderNo;
  public Long userId;
  public Long planId;
  public Integer amount;
  public String status;
  public String channel;
  public String requestKey;
  public String transactionId;
  public LocalDateTime createdAt;
  public LocalDateTime paidAt;
  public LocalDateTime expiredAt;

  public Long getId() {
    return id;
  }

  public String getOrderNo() {
    return orderNo;
  }

  public Long getUserId() {
    return userId;
  }

  public String getStatus() {
    return status;
  }

  public String getRequestKey() {
    return requestKey;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getPaidAt() {
    return paidAt;
  }

  public LocalDateTime getExpiredAt() {
    return expiredAt;
  }
}
