package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.AuthResponse;
import cn.resumecraft.dto.CredentialsRequest;
import cn.resumecraft.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  ApiResponse<AuthResponse> register(@Valid @RequestBody CredentialsRequest request) {
    return ApiResponse.ok(authService.register(request));
  }

  @PostMapping("/login")
  ApiResponse<AuthResponse> login(@Valid @RequestBody CredentialsRequest request) {
    return ApiResponse.ok(authService.login(request));
  }
}
