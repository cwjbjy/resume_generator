package cn.resumecraft.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class TokenServiceTest {
  @Test
  void tokensCreatedBeforeSessionTrackingRequireAnotherLogin() {
    String secret = "test-secret-for-legacy-session-token-validation";
    RedisSessionService sessions = mock(RedisSessionService.class);
    TokenService tokens = new TokenService(secret, 2, sessions);
    String legacy =
        Jwts.builder()
            .subject("42")
            .claim("username", "developer01")
            .expiration(Date.from(Instant.now().plusSeconds(60)))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .compact();

    assertThatThrownBy(() -> tokens.parse(legacy)).isInstanceOf(JwtException.class);
    verifyNoInteractions(sessions);
  }

  @Test
  void aTokenThatExpiresDuringLogoutDoesNotRaiseAServerError() {
    String secret = "test-secret-for-expired-session-token-validation";
    RedisSessionService sessions = mock(RedisSessionService.class);
    TokenService tokens = new TokenService(secret, 2, sessions);
    String expired =
        Jwts.builder()
            .subject("42")
            .id("expired_session")
            .expiration(Date.from(Instant.now().minusSeconds(1)))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .compact();

    tokens.revoke(expired);

    verifyNoInteractions(sessions);
  }
}
