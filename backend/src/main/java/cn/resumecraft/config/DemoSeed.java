package cn.resumecraft.config;

import cn.resumecraft.mapper.UserMapper;
import cn.resumecraft.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DemoSeed implements CommandLineRunner {
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public DemoSeed(UserMapper userMapper, PasswordEncoder passwordEncoder) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
  }

  public void run(String... args) {
    for (String name : new String[] {"user01", "vip01"}) {
      if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, name)) > 0) {
        continue;
      }
      User user = new User();
      user.username = name;
      user.nickname = name.equals("vip01") ? "会员体验账号" : "求职者";
      user.passwordHash = passwordEncoder.encode("123456");
      user.email = name + "@example.com";
      user.createdAt = LocalDateTime.now();
      if (name.equals("vip01")) user.memberExpireAt = LocalDateTime.now().plusDays(365);
      userMapper.insert(user);
    }
  }
}
