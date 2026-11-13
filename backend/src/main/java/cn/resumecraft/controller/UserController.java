package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.MembershipResponse;
import cn.resumecraft.dto.UpdateProfileRequest;
import cn.resumecraft.entity.User;
import cn.resumecraft.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping("/users/me")
  ApiResponse<User> current(Authentication authentication) {
    return ApiResponse.ok(userService.current((Long) authentication.getPrincipal()));
  }

  @PutMapping("/users/me")
  ApiResponse<User> update(
      Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
    return ApiResponse.ok(userService.updateProfile((Long) authentication.getPrincipal(), request));
  }

  @GetMapping("/membership")
  ApiResponse<MembershipResponse> membership(Authentication authentication) {
    return ApiResponse.ok(userService.membership((Long) authentication.getPrincipal()));
  }
}
