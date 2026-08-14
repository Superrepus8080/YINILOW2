package com.yinilow.auth;

import io.vertx.core.json.JsonObject;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SellerAuthService {
  private final SecureRandom random = new SecureRandom();
  private final Map<String, SellerSession> sessions = new ConcurrentHashMap<>();
  private final String accessPin;

  public SellerAuthService() {
    this.accessPin = System.getenv().getOrDefault("YINILOW_SELLER_ACCESS_PIN", "yinilow-demo");
  }

  public AuthResult login(JsonObject request) {
    String pin = request.getString("pin", "");
    if (!accessPin.equals(pin)) {
      return AuthResult.failure(401, "INVALID_SELLER_PIN");
    }
    String token = token();
    SellerSession session = new SellerSession("seller_demo", "YINILOW Seller", token);
    sessions.put(token, session);
    return AuthResult.success(session.toJson());
  }

  public Optional<SellerSession> authenticate(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return Optional.empty();
    }
    return Optional.ofNullable(sessions.get(authorizationHeader.substring("Bearer ".length()).trim()));
  }

  public void logout(String authorizationHeader) {
    authenticate(authorizationHeader).ifPresent(session -> sessions.remove(session.token()));
  }

  private String token() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public record SellerSession(String sellerId, String displayName, String token) {
    public JsonObject toJson() {
      return new JsonObject()
        .put("sellerId", sellerId)
        .put("displayName", displayName)
        .put("token", token);
    }
  }

  public record AuthResult(boolean success, int statusCode, JsonObject payload) {
    public static AuthResult success(JsonObject payload) {
      return new AuthResult(true, 200, payload);
    }

    public static AuthResult failure(int statusCode, String errorCode) {
      return new AuthResult(false, statusCode, new JsonObject().put("error", errorCode));
    }
  }
}
