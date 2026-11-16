package cn.resumecraft.dto;

import cn.resumecraft.entity.User;
import java.time.LocalDateTime;

public record UserProfileResponse(
    Long id,
    String username,
    String nickname,
    String email,
    String avatarUrl,
    boolean member,
    LocalDateTime memberExpireAt,
    LocalDateTime createdAt) {
  public static UserProfileResponse from(User user) {
    return new UserProfileResponse(
        user.id,
        user.username,
        user.nickname,
        user.email,
        user.avatarUrl,
        user.isMember(),
        user.memberExpireAt,
        user.createdAt);
  }
}
