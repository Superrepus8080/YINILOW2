package com.yinilow.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DatabaseConfigTest {
  @Test
  void convertsRenderDatabaseUrlToJdbcUrl() {
    DatabaseConfig config = DatabaseConfig.fromRenderUrl("postgres://user:pass@host.render.com:5432/yinilow");

    assertEquals("jdbc:postgresql://host.render.com:5432/yinilow?sslmode=require", config.jdbcUrl());
    assertEquals("user", config.username());
    assertEquals("pass", config.password());
  }

  @Test
  void keepsExplicitQueryString() {
    DatabaseConfig config = DatabaseConfig.fromRenderUrl("postgres://u:p@host/yinilow?sslmode=disable");

    assertEquals("jdbc:postgresql://host/yinilow?sslmode=disable", config.jdbcUrl());
  }
}
