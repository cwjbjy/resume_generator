package cn.resumecraft.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cn.resumecraft.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

class DemoSeedTest {
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(DemoSeed.class)
          .withBean(UserMapper.class, () -> mock(UserMapper.class))
          .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class));

  @Test
  void enabledInDev() {
    contextRunner
        .withPropertyValues("spring.profiles.active=dev")
        .run(context -> assertThat(context).hasSingleBean(DemoSeed.class));
  }

  @Test
  void disabledInProd() {
    contextRunner
        .withPropertyValues("spring.profiles.active=prod")
        .run(context -> assertThat(context).doesNotHaveBean(DemoSeed.class));
  }
}
