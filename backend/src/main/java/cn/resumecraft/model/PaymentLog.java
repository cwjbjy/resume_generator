package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@TableName("payment_logs")
public class PaymentLog {
  @TableId public Long id;
  public Long orderId;
  public String channel;
  public String rawBody;
  public String result;
  public LocalDateTime createdAt;
}
