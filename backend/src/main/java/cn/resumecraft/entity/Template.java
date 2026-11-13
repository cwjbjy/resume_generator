package cn.resumecraft.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;

@Getter
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
}
