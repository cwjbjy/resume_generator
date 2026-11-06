package cn.resumecraft;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class MySqlTestConfiguration {
  @Bean
  @ServiceConnection
  MySQLContainer<?> mysql() {
    return new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("resumecraft_test")
        .withEnv("TZ", "Asia/Shanghai")
        .withUrlParam("serverTimezone", "Asia/Shanghai")
        .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");
  }
}
