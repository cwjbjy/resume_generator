package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.AuthResponse;
import cn.resumecraft.dto.CredentialsRequest;
import cn.resumecraft.security.LoginClientAddressResolver;
import cn.resumecraft.security.LoginRateLimitException;
import cn.resumecraft.security.LoginRateLimiter;
import cn.resumecraft.security.TokenService;
import cn.resumecraft.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final LoginRateLimiter loginRateLimiter;
  private final TokenService tokenService;
  private final LoginClientAddressResolver clientAddresses;

  @PostMapping("/register")
  ApiResponse<AuthResponse> register(@Valid @RequestBody CredentialsRequest request) {
    return ApiResponse.ok(authService.register(request));
  }

  @PostMapping("/login")
  ApiResponse<AuthResponse> login(
      @Valid @RequestBody CredentialsRequest request, HttpServletRequest servletRequest) {
    loginRateLimiter.acquire(clientAddresses.resolve(servletRequest), request.username());
    AuthResponse response = authService.login(request);
    return ApiResponse.ok(response);
  }

  @PostMapping("/logout")
  ApiResponse<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    tokenService.revoke(authorization.substring(7));
    return ApiResponse.ok(null);
  }

  @ExceptionHandler(LoginRateLimitException.class)
  ResponseEntity<ApiResponse<Void>> rateLimited(LoginRateLimitException exception) {
    return ResponseEntity.status(429)
        .header(HttpHeaders.RETRY_AFTER, Long.toString(exception.retryAfterSeconds()))
        .body(new ApiResponse<>(429, exception.getMessage(), null));
  }
}
