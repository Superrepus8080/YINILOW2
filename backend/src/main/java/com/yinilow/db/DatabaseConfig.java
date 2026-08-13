package com.yinilow.db;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public record DatabaseConfig(String jdbcUrl, String username, String password) {
  public static DatabaseConfig fromEnvironment() {
    String databaseUrl = System.getenv("DATABASE_URL");
    if (databaseUrl == null || databaseUrl.isBlank()) {
      return null;
    }
    return fromRenderUrl(databaseUrl);
  }

  static DatabaseConfig fromRenderUrl(String databaseUrl) {
    try {
      URI uri = new URI(databaseUrl);
      String userInfo = uri.getUserInfo();
      String username = "";
      String password = "";
      if (userInfo != null) {
        String[] parts = userInfo.split(":", 2);
        username = decode(parts[0]);
        if (parts.length > 1) {
          password = decode(parts[1]);
        }
      }

      String query = withTimeouts(uri.getQuery());
      String port = uri.getPort() > 0 ? ":" + uri.getPort() : "";
      String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + port + uri.getPath()
        + "?" + query;
      return new DatabaseConfig(jdbcUrl, username, password);
    } catch (URISyntaxException exception) {
      throw new IllegalArgumentException("DATABASE_URL is not a valid URL", exception);
    }
  }

  private static String decode(String value) {
    return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  private static String withTimeouts(String query) {
    String base = query == null || query.isBlank() ? "sslmode=require" : query;
    if (!base.contains("connectTimeout=")) {
      base += "&connectTimeout=10";
    }
    if (!base.contains("socketTimeout=")) {
      base += "&socketTimeout=20";
    }
    return base;
  }
}
