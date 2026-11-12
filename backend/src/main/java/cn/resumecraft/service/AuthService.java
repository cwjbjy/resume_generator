package cn.resumecraft.service;

import static cn.resumecraft.api.BizException.require;

import cn.resumecraft.dto.AuthResponse;
import cn.resumecraft.dto.CredentialsRequest;
import cn.resumecraft.mapper.UserMapper;
import cn.resumecraft.model.User;
import cn.resumecraft.security.TokenService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private static final String UNKNOWN_USER_HASH =
      "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  public AuthService(
      UserMapper userMapper, PasswordEncoder passwordEncoder, TokenService tokenService) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
  }

  public AuthResponse register(CredentialsRequest credentials) {
    validatePasswordLength(credentials.password());
    require(
        userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, credentials.username()))
            == 0,
        409,
        "用户名已被使用");
    User user = new User();
    user.username = credentials.username();
    user.nickname = credentials.username();
    user.email = credentials.email();
    user.passwordHash = passwordEncoder.encode(credentials.password());
    user.createdAt = LocalDateTime.now();
    userMapper.insert(user);
    return authenticated(user);
  }

  public AuthResponse login(CredentialsRequest credentials) {
    validatePasswordLength(credentials.password());
    User user =
        userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, credentials.username()));
    // Keep a BCrypt check on unknown usernames to avoid a timing-based distinction.
    String passwordHash = user == null ? UNKNOWN_USER_HASH : user.passwordHash;
    boolean passwordMatches = passwordEncoder.matches(credentials.password(), passwordHash);
    require(user != null && passwordMatches, 401, "用户名或密码错误");
    return authenticated(user);
  }

  private AuthResponse authenticated(User user) {
    return new AuthResponse(tokenService.create(user.id, user.username), user);
  }

  private void validatePasswordLength(String password) {
    require(password.getBytes(StandardCharsets.UTF_8).length <= 72, 400, "密码 UTF-8 编码不能超过 72 字节");
  }
}
