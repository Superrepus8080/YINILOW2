package com.yinilow.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
  private final DatabaseConfig config;

  public Database(DatabaseConfig config) {
    this.config = config;
  }

  public Connection connection() throws SQLException {
    DriverManager.setLoginTimeout(10);
    return DriverManager.getConnection(config.jdbcUrl(), config.username(), config.password());
  }
}
