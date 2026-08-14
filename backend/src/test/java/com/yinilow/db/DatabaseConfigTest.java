package com.yinilow.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class DatabaseConfigTest {
  @Test
  void convertsRenderDatabaseUrlToJdbcUrl() {
    DatabaseConfig config = DatabaseConfig.fromRenderUrl("postgres://user:pass@host.render.com:5432/yinilow");

    assertEquals("jdbc:postgresql://host.render.com:5432/yinilow?sslmode=require&connectTimeout=10&socketTimeout=20", config.jdbcUrl());
    assertEquals("user", config.username());
    assertEquals("pass", config.password());
  }

  @Test
  void keepsExplicitQueryString() {
    DatabaseConfig config = DatabaseConfig.fromRenderUrl("postgres://u:p@host/yinilow?sslmode=disable");

    assertEquals("jdbc:postgresql://host/yinilow?sslmode=disable&connectTimeout=10&socketTimeout=20", config.jdbcUrl());
  }

  @Test
  void seedSqlFormatsWithCleanupPatterns() throws Exception {
    Method seedSql = DatabaseBootstrap.class.getDeclaredMethod("seedSql");
    seedSql.setAccessible(true);

    String sql = (String) seedSql.invoke(null);

    assertTrue(sql.contains("lst_uploaded-photo-test-%"));
    assertTrue(sql.contains("lst_black_bomber_jacket"));
  }
}
