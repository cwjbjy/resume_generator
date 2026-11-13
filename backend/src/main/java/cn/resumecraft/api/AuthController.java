package cn.resumecraft.api;

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
  Api<AuthResponse> register(@Valid @RequestBody CredentialsRequest request) {
    return Api.ok(authService.register(request));
  }

  @PostMapping("/login")
  Api<AuthResponse> login(@Valid @RequestBody CredentialsRequest request) {
    return Api.ok(authService.login(request));
  }
}
