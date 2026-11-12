package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("plans")
public class Plan {
  @TableId public Long id;

  public String name;
  public Integer price;
  public Integer durationDays;
  public String features;
  public Integer sort;

  public Integer getSort() {
    return sort;
  }
}
