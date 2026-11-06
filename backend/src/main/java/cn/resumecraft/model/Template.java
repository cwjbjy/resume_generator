package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("templates")
public class Template {
  @TableId(type = IdType.INPUT)
  public Long id;

  public String name;
  public String category;
  public Boolean isVip;
  public String color;
  public String style;
  public Integer sort;
  public Integer usedCount;
}
