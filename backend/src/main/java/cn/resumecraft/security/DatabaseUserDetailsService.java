package cn.resumecraft.security;

import cn.resumecraft.entity.User;
import cn.resumecraft.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {
  private final UserMapper userMapper;

  @Override
  public AccountDetails loadUserByUsername(String username) {
    User user =
        userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    if (user == null) {
      throw new UsernameNotFoundException("用户名或密码错误");
    }
    return AccountDetails.from(user);
  }
}
