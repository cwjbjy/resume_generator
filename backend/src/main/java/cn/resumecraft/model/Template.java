package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("templates")
public class Template {
  @TableId public Long id;

  public String name;
  public String category;
  public Boolean isVip;
  public String color;
  public String style;
  public Integer sort;
  public Integer usedCount;

  public Long getId() {
    return id;
  }

  public Boolean getIsVip() {
    return isVip;
  }

  public Integer getSort() {
    return sort;
  }

  public Integer getUsedCount() {
    return usedCount;
  }
}
