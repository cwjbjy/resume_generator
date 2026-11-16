package cn.resumecraft.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  private final SecretKey signingKey;
  private final long lifetimeHours;
  private final RedisSessionService sessions;

  public TokenService(
      @Value("${app.jwt-secret}") String secret,
      @Value("${app.jwt-hours}") long lifetimeHours,
      RedisSessionService sessions) {
    if (secret.length() < 32) {
      throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
    }
    signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.lifetimeHours = lifetimeHours;
    if (lifetimeHours < 1) throw new IllegalArgumentException("JWT 有效期必须大于零");
    this.sessions = sessions;
  }

  public String create(Long userId, String username) {
    String sessionId = UUID.randomUUID().toString();
    String token =
        Jwts.builder()
            .subject(userId.toString())
            .id(sessionId)
            .claim("username", username)
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plusSeconds(lifetimeHours * 3600)))
            .signWith(signingKey)
            .compact();
    sessions.replace(userId, username, sessionId, Duration.ofHours(lifetimeHours));
    return token;
  }

  public LoginUser parse(String token) {
    Claims claims =
        Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    Long userId = Long.valueOf(claims.getSubject());
    String sessionId = claims.getId();
    if (sessionId == null || sessionId.isBlank() || !sessions.isCurrent(userId, sessionId)) {
      throw new JwtException("登录状态已更新，请重新登录");
    }
    return new LoginUser(userId, claims.get("username", String.class));
  }

  public void revoke(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      sessions.revoke(Long.valueOf(claims.getSubject()), claims.getId());
    } catch (ExpiredJwtException expiredToken) {
      // The token may expire after the authentication filter has accepted the request.
    }
  }
}
