package cn.resumecraft.config;

import cn.resumecraft.security.JwtAuthenticationFilter;
import cn.resumecraft.security.TokenService;
import cn.resumecraft.security.UnauthorizedEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  private static final String[] PUBLIC_ENDPOINTS = {
    "/api/auth/register",
    "/api/auth/login",
    "/api/templates/**",
    "/api/plans",
    "/api/config",
    "/api/pay/callback",
    "/actuator/health",
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/doc.html",
    "/webjars/**",
    "/error"
  };

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      TokenService tokenService,
      UnauthorizedEntryPoint unauthorizedEntryPoint,
      ObjectMapper objectMapper)
      throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            requests ->
                requests.requestMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().authenticated())
        .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthorizedEntryPoint))
        .addFilterBefore(
            new JwtAuthenticationFilter(tokenService, objectMapper),
            UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
