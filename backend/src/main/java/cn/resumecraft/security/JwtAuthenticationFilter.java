package cn.resumecraft.security;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.exception.BizException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final TokenService tokenService;
  private final ObjectMapper objectMapper;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return path.equals("/api/auth/login") || path.equals("/api/auth/register");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      try {
        LoginUser loginUser = tokenService.parse(authorization.substring(7));
        SecurityContextHolder.getContext()
            .setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(loginUser, null, List.of()));
      } catch (JwtException | IllegalArgumentException invalidToken) {
        SecurityContextHolder.clearContext();
      } catch (BizException unavailable) {
        SecurityContextHolder.clearContext();
        response.setStatus(unavailable.status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(
            response.getWriter(),
            new ApiResponse<>(unavailable.status, unavailable.getMessage(), null));
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
