package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;

@Getter
@TableName("plans")
public class Plan {
  @TableId public Long id;

  public String name;
  public Integer price;
  public Integer durationDays;
  public String features;
  public Integer sort;
}
