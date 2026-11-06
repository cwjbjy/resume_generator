package cn.resumecraft.api;

import cn.resumecraft.dto.MembershipResponse;
import cn.resumecraft.dto.UpdateProfileRequest;
import cn.resumecraft.model.User;
import cn.resumecraft.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users/me")
  Api<User> current(Authentication authentication) {
    return Api.ok(userService.current((Long) authentication.getPrincipal()));
  }

  @PutMapping("/users/me")
  Api<User> update(
      Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
    return Api.ok(userService.updateProfile((Long) authentication.getPrincipal(), request));
  }

  @GetMapping("/membership")
  Api<MembershipResponse> membership(Authentication authentication) {
    return Api.ok(userService.membership((Long) authentication.getPrincipal()));
  }
}
