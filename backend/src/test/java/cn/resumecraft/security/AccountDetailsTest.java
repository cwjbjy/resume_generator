package cn.resumecraft.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import cn.resumecraft.dto.AuthResponse;
import cn.resumecraft.dto.CredentialsRequest;
import cn.resumecraft.entity.User;
import cn.resumecraft.mapper.UserMapper;
import cn.resumecraft.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AccountDetailsTest {
  @Test
  void loginMapsIndependentAuthenticationFieldsWithOneUserQuery() {
    BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    String passwordHash = passwords.encode("test123456");
    User user = new User();
    user.id = 42L;
    user.username = "developer01";
    user.passwordHash = passwordHash;
    UserMapper users = mock(UserMapper.class);
    when(users.selectOne(any())).thenReturn(user);
    DaoAuthenticationProvider provider =
        new DaoAuthenticationProvider(new DatabaseUserDetailsService(users));
    provider.setPasswordEncoder(passwords);
    provider.setPostAuthenticationChecks(
        details -> {
          user.id = 99L;
          user.username = "updated01";
          user.passwordHash = "changed";
          assertThat(details.getUsername()).isEqualTo("developer01");
          assertThat(details.getPassword()).isEqualTo(passwordHash);
        });
    TokenService tokens = new TokenService("test-jwt-secret-for-account-details-mapping", 2);
    AuthService auth = new AuthService(new ProviderManager(provider), users, passwords, tokens);

    AuthResponse response = auth.login(new CredentialsRequest("developer01", "test123456", null));

    verify(users).selectOne(any());
    verifyNoMoreInteractions(users);
    assertThat(user.id).isEqualTo(99L);
    assertThat(response.id()).isEqualTo(42L);
    assertThat(response.username()).isEqualTo("developer01");
    assertThat(tokens.parse(response.token())).isEqualTo(new LoginUser(42L, "developer01"));
  }
}
