package cn.resumecraft.service;

import static cn.resumecraft.exception.BizException.require;

import cn.resumecraft.dto.AuthResponse;
import cn.resumecraft.dto.CredentialsRequest;
import cn.resumecraft.entity.User;
import cn.resumecraft.mapper.UserMapper;
import cn.resumecraft.security.AccountDetails;
import cn.resumecraft.security.TokenService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final AuthenticationManager authenticationManager;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

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
    return new AuthResponse(tokenService.create(user.id, user.username), user.id, user.username);
  }

  public AuthResponse login(CredentialsRequest credentials) {
    validatePasswordLength(credentials.password());
    Authentication authentication =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                credentials.username(), credentials.password()));
    AccountDetails account = (AccountDetails) authentication.getPrincipal();
    return new AuthResponse(
        tokenService.create(account.getId(), account.getUsername()),
        account.getId(),
        account.getUsername());
  }

  private void validatePasswordLength(String password) {
    require(password.getBytes(StandardCharsets.UTF_8).length <= 72, 400, "密码 UTF-8 编码不能超过 72 字节");
  }
}
