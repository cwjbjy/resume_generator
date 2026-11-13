package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
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
}
