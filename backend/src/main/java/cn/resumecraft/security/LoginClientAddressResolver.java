package cn.resumecraft.security;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

@Component
public class LoginClientAddressResolver {
  private final List<IpAddressMatcher> trustedProxies;

  public LoginClientAddressResolver(
      @Value("${app.security.login-limit.trusted-proxies:}") String trustedProxies) {
    this.trustedProxies =
        Arrays.stream(trustedProxies.split(","))
            .map(String::trim)
            .filter(address -> !address.isEmpty())
            .map(IpAddressMatcher::new)
            .toList();
  }

  public String resolve(HttpServletRequest request) {
    String remote = request.getRemoteAddr();
    if (trustedProxies.stream().noneMatch(proxy -> proxy.matches(remote))) return remote;
    String forwarded = request.getHeader("X-Real-IP");
    if (forwarded == null
        || !(forwarded.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")
            || (forwarded.contains(":") && forwarded.matches("[0-9a-fA-F:]+")))) {
      return remote;
    }
    try {
      return InetAddress.getByName(forwarded).getHostAddress();
    } catch (UnknownHostException invalidAddress) {
      return remote;
    }
  }
}
