package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@TableName("users")
public class User {
  @TableId public Long id;
  public String username;
  @JsonIgnore public String passwordHash;
  public String email;
  public String nickname;
  public String avatarUrl;
  public LocalDateTime memberExpireAt;
  public LocalDateTime createdAt;

  public boolean isMember() {
    return memberExpireAt != null && memberExpireAt.isAfter(LocalDateTime.now());
  }
}
