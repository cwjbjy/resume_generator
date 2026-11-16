package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;

class OrderSnapshotMigrationTest {
  @Test
  void existingOrdersReceiveSnapshotsWithoutRewritingPreviousMigrations() throws Exception {
    try (var mysql = new MySQLContainer<>("mysql:8.0")) {
      mysql.start();
      Flyway.configure()
          .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
          .locations("classpath:db/migration/mysql")
          .target("2")
          .load()
          .migrate();
      try (var connection =
              DriverManager.getConnection(
                  mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
          var statement = connection.createStatement()) {
        statement.executeUpdate(
            "INSERT INTO users(username, password_hash) VALUES ('legacy_user', 'test_hash')");
        statement.executeUpdate(
            """
            INSERT INTO orders(order_no, user_id, plan_id, amount, request_key, expired_at)
            SELECT 'legacy_order', u.id, p.id, 1500, 'legacy_key', CURRENT_TIMESTAMP
            FROM users u CROSS JOIN plans p
            WHERE u.username = 'legacy_user'
            ORDER BY p.sort LIMIT 1
            """);
        statement.executeUpdate(
            """
            INSERT INTO export_logs(user_id, resume_id, template_id, status)
            SELECT id, 1, 1, 'COMPLETED' FROM users WHERE username = 'legacy_user'
            """);
        var flyway =
            Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .locations("classpath:db/migration/mysql")
                .target("3")
                .load();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        try (var result =
            statement.executeQuery(
                """
                SELECT o.plan_name, o.duration_days, o.amount, p.name, p.duration_days
                FROM orders o JOIN plans p ON p.id = o.plan_id
                WHERE o.order_no = 'legacy_order'
                """)) {
          assertThat(result.next()).isTrue();
          assertThat(result.getString(1)).isEqualTo(result.getString(4));
          assertThat(result.getInt(2)).isEqualTo(result.getInt(5));
          assertThat(result.getInt(3)).isEqualTo(1500);
        }
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        var latest =
            Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .locations("classpath:db/migration/mysql")
                .load();
        assertThat(latest.migrate().migrationsExecuted).isEqualTo(1);
        try (var result =
            statement.executeQuery(
                "SELECT content_snapshot, resume_version, status FROM export_logs")) {
          assertThat(result.next()).isTrue();
          assertThat(result.getString("content_snapshot")).isNull();
          assertThat(result.getObject("resume_version")).isNull();
          assertThat(result.getString("status")).isEqualTo("COMPLETED");
        }
        assertThat(latest.migrate().migrationsExecuted).isZero();
      }
    }
  }
}
