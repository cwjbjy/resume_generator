package cn.resumecraft.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  private final SecretKey signingKey;
  private final long lifetimeHours;

  public TokenService(
      @Value("${app.jwt-secret}") String secret, @Value("${app.jwt-hours}") long lifetimeHours) {
    if (secret.length() < 32) {
      throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
    }
    signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.lifetimeHours = lifetimeHours;
  }

  public String create(Long userId, String username) {
    return Jwts.builder()
        .subject(userId.toString())
        .claim("username", username)
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().plusSeconds(lifetimeHours * 3600)))
        .signWith(signingKey)
        .compact();
  }

  public Long parse(String token) {
    return Long.valueOf(
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject());
  }
}
