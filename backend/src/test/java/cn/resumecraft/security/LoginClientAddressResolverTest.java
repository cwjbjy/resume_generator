package cn.resumecraft.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class LoginClientAddressResolverTest {
  @Test
  void forwardingHeadersAreIgnoredByDefault() {
    var request = new MockHttpServletRequest();
    request.setRemoteAddr("198.51.100.20");
    request.addHeader("X-Real-IP", "203.0.113.9");
    request.addHeader("X-Forwarded-For", "203.0.113.10");
    assertThat(new LoginClientAddressResolver("").resolve(request)).isEqualTo("198.51.100.20");
  }

  @Test
  void onlyAnExplicitTrustedProxyCanSupplyTheClientAddress() {
    var request = new MockHttpServletRequest();
    request.setRemoteAddr("172.28.0.4");
    request.addHeader("X-Real-IP", "203.0.113.9");
    assertThat(new LoginClientAddressResolver("172.28.0.0/24").resolve(request))
        .isEqualTo("203.0.113.9");
    assertThat(new LoginClientAddressResolver("127.0.0.1").resolve(request))
        .isEqualTo("172.28.0.4");
  }

  @Test
  void invalidForwardedAddressesFallBackToTheProxyAddress() {
    var request = new MockHttpServletRequest();
    request.setRemoteAddr("127.0.0.1");
    request.addHeader("X-Real-IP", "attacker.example.com");
    assertThat(new LoginClientAddressResolver("127.0.0.1").resolve(request)).isEqualTo("127.0.0.1");
  }

  @Test
  void trustedProxiesSupportIpv6Clients() {
    var request = new MockHttpServletRequest();
    request.setRemoteAddr("127.0.0.1");
    request.addHeader("X-Real-IP", "2001:db8::1");
    assertThat(new LoginClientAddressResolver("127.0.0.1").resolve(request))
        .isEqualTo("2001:db8:0:0:0:0:0:1");
  }
}
