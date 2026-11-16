package cn.resumecraft.security;

import java.security.Principal;

public record LoginUser(Long userId, String username) implements Principal {
  @Override
  public String getName() {
    return username;
  }
}
