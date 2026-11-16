package cn.resumecraft.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {
  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void exposesUserIdAndUsernameFromVerifiedToken() throws Exception {
    TokenService tokenService = new TokenService("test-jwt-secret-for-login-user-principal", 2);
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
    request.addHeader("Authorization", "Bearer " + tokenService.create(42L, "developer01"));
    AtomicBoolean chainInvoked = new AtomicBoolean();

    filter.doFilter(
        request,
        new MockHttpServletResponse(),
        (servletRequest, servletResponse) -> {
          chainInvoked.set(true);
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          assertThat(authentication).isNotNull();
          assertThat(authentication.isAuthenticated()).isTrue();
          assertThat(authentication.getPrincipal())
              .isInstanceOfSatisfying(
                  LoginUser.class,
                  loginUser -> {
                    assertThat(loginUser.userId()).isEqualTo(42L);
                    assertThat(loginUser.username()).isEqualTo("developer01");
                  });
          assertThat(authentication.getName()).isEqualTo("developer01");
        });

    assertThat(chainInvoked).isTrue();
  }
}
