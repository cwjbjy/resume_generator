package cn.resumecraft.security;

import cn.resumecraft.entity.User;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountDetails implements UserDetails {
  private final Long id;
  private final String username;
  private final String password;

  public static AccountDetails from(User user) {
    return new AccountDetails(user.id, user.username, user.passwordHash);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }
}
