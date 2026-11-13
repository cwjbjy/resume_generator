package cn.resumecraft.service;

import static cn.resumecraft.api.BizException.require;

import cn.resumecraft.dto.MembershipResponse;
import cn.resumecraft.dto.UpdateProfileRequest;
import cn.resumecraft.mapper.UserMapper;
import cn.resumecraft.model.User;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserMapper userMapper;

  public User current(Long userId) {
    User user = userMapper.selectById(userId);
    require(user != null, 401, "账号不存在");
    return user;
  }

  public User updateProfile(Long userId, UpdateProfileRequest profile) {
    current(userId);
    userMapper.update(
        null,
        new LambdaUpdateWrapper<User>()
            .eq(User::getId, userId)
            .set(User::getNickname, profile.nickname())
            .set(User::getEmail, profile.email()));
    return current(userId);
  }

  public MembershipResponse membership(Long userId) {
    User user = current(userId);
    return new MembershipResponse(
        user.isMember(), user.memberExpireAt == null ? "" : user.memberExpireAt.toString());
  }
}
