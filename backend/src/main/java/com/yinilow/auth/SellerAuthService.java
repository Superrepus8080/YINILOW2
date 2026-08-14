package com.yinilow.auth;

import com.yinilow.db.Database;
import io.vertx.core.json.JsonObject;
import org.mindrot.jbcrypt.BCrypt;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SellerAuthService {
  private final SecureRandom random = new SecureRandom();
  private final Map<String, SellerSession> sessions = new ConcurrentHashMap<>();
  private final String demoPin = System.getenv().getOrDefault("YINILOW_SELLER_ACCESS_PIN", "yinilow-demo");
  private volatile Database database;

  public void useDatabase(Database database) {
    this.database = database;
    ensureDemoSeller();
  }

  public AuthResult register(JsonObject request) {
    if (database == null) {
      return memoryLogin(request.getString("displayName", "YINILOW Seller"), request.getString("email", "seller@yinilow.local"));
    }
    String email = email(request.getString("email"));
    String displayName = clean(request.getString("displayName"));
    String password = request.getString("password", "");
    if (email.isBlank() || displayName.isBlank() || password.length() < 6) {
      return AuthResult.failure(400, "SELLER_ACCOUNT_INVALID");
    }
    try (Connection connection = database.connection();
         var statement = connection.prepareStatement("""
           INSERT INTO auth.seller_accounts (email, display_name, password_hash)
           VALUES (?, ?, ?)
           RETURNING id::text, email, display_name
           """)) {
      statement.setString(1, email);
      statement.setString(2, displayName);
      statement.setString(3, passwordHash(password));
      try (ResultSet row = statement.executeQuery()) {
        row.next();
        return AuthResult.success(createPersistentSession(connection, row.getString("id"), row.getString("email"), row.getString("display_name")).toJson());
      }
    } catch (SQLException exception) {
      if ("23505".equals(exception.getSQLState())) {
        return AuthResult.failure(409, "SELLER_EMAIL_EXISTS");
      }
      throw new IllegalStateException("Could not create seller account", exception);
    }
  }

  public AuthResult login(JsonObject request) {
    if (request.containsKey("pin")) {
      return legacyPinLogin(request);
    }
    if (database == null) {
      return memoryLogin("YINILOW Seller", email(request.getString("email", "seller@yinilow.local")));
    }
    String email = email(request.getString("email"));
    String password = request.getString("password", "");
    try (Connection connection = database.connection();
         var statement = connection.prepareStatement("""
           SELECT id::text, email, display_name, password_hash
           FROM auth.seller_accounts
           WHERE email = ? AND status = 'ACTIVE'
           LIMIT 1
           """)) {
      statement.setString(1, email);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return AuthResult.failure(401, "INVALID_SELLER_LOGIN");
        }
        String sellerId = row.getString("id");
        String storedHash = row.getString("password_hash");
        if (!passwordMatches(password, storedHash)) {
          return AuthResult.failure(401, "INVALID_SELLER_LOGIN");
        }
        if (!isBcryptHash(storedHash)) {
          upgradePasswordHash(connection, sellerId, password);
        }
        return AuthResult.success(createPersistentSession(connection, row.getString("id"), row.getString("email"), row.getString("display_name")).toJson());
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not sign in seller", exception);
    }
  }

  public Optional<SellerSession> authenticate(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return Optional.empty();
    }
    String token = authorizationHeader.substring("Bearer ".length()).trim();
    if (database == null) {
      return Optional.ofNullable(sessions.get(token));
    }
    try (Connection connection = database.connection();
         var statement = connection.prepareStatement("""
           SELECT seller.id::text, seller.email, seller.display_name
           FROM auth.seller_sessions session
           JOIN auth.seller_accounts seller ON seller.id = session.seller_account_id
           WHERE session.token_hash = ?
             AND session.revoked_at IS NULL
             AND session.expires_at > now()
             AND seller.status = 'ACTIVE'
           LIMIT 1
           """)) {
      statement.setString(1, tokenHash(token));
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return Optional.empty();
        }
        return Optional.of(new SellerSession(row.getString("id"), row.getString("email"), row.getString("display_name"), token));
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not authenticate seller session", exception);
    }
  }

  public void logout(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return;
    }
    String token = authorizationHeader.substring("Bearer ".length()).trim();
    sessions.remove(token);
    if (database == null) {
      return;
    }
    try (Connection connection = database.connection();
         var statement = connection.prepareStatement("""
           UPDATE auth.seller_sessions
           SET revoked_at = now()
           WHERE token_hash = ? AND revoked_at IS NULL
           """)) {
      statement.setString(1, tokenHash(token));
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not end seller session", exception);
    }
  }

  private AuthResult legacyPinLogin(JsonObject request) {
    String pin = request.getString("pin", "");
    if (!demoPin.equals(pin)) {
      return AuthResult.failure(401, "INVALID_SELLER_PIN");
    }
    if (database != null) {
      return login(new JsonObject()
        .put("email", "seller@yinilow.local")
        .put("password", demoPin));
    }
    return memoryLogin("YINILOW Seller", "seller@yinilow.local");
  }

  private AuthResult memoryLogin(String displayName, String email) {
    return AuthResult.success(createSession("seller_demo", email, displayName).toJson());
  }

  private SellerSession createSession(String sellerId, String email, String displayName) {
    SellerSession session = new SellerSession(sellerId, email, displayName, token());
    sessions.put(session.token(), session);
    return session;
  }

  private SellerSession createPersistentSession(Connection connection, String sellerId, String email, String displayName) throws SQLException {
    SellerSession session = new SellerSession(sellerId, email, displayName, token());
    try (var statement = connection.prepareStatement("""
      INSERT INTO auth.seller_sessions (token_hash, seller_account_id, expires_at)
      VALUES (?, ?::uuid, now() + interval '30 days')
      """)) {
      statement.setString(1, tokenHash(session.token()));
      statement.setString(2, sellerId);
      statement.executeUpdate();
    }
    return session;
  }

  private void ensureDemoSeller() {
    try (Connection connection = database.connection();
         var statement = connection.prepareStatement("""
           INSERT INTO auth.seller_accounts (email, display_name, password_hash)
           VALUES ('seller@yinilow.local', 'YINILOW Seller', ?)
           ON CONFLICT (email) DO UPDATE
           SET password_hash = EXCLUDED.password_hash,
             updated_at = now()
           WHERE auth.seller_accounts.password_hash = ''
             OR auth.seller_accounts.password_hash NOT LIKE '$2%'
           """)) {
      statement.setString(1, passwordHash(demoPin));
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not seed demo seller", exception);
    }
  }

  private String token() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String passwordHash(String value) {
    return BCrypt.hashpw(value, BCrypt.gensalt(12));
  }

  private static boolean passwordMatches(String password, String storedHash) {
    if (isBcryptHash(storedHash)) {
      return BCrypt.checkpw(password, storedHash);
    }
    return legacySha256(password).equals(storedHash);
  }

  private static boolean isBcryptHash(String value) {
    return value != null && value.startsWith("$2");
  }

  private static void upgradePasswordHash(Connection connection, String sellerId, String password) throws SQLException {
    try (var statement = connection.prepareStatement("""
      UPDATE auth.seller_accounts
      SET password_hash = ?, updated_at = now()
      WHERE id = ?::uuid
      """)) {
      statement.setString(1, passwordHash(password));
      statement.setString(2, sellerId);
      statement.executeUpdate();
    }
  }

  private static String legacySha256(String value) {
    return sha256Base64(value);
  }

  private static String tokenHash(String value) {
    return sha256Base64(value);
  }

  private static String sha256Base64(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hashed);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("Missing SHA-256 support", exception);
    }
  }

  private static String email(String value) {
    return clean(value).toLowerCase(Locale.ROOT);
  }

  private static String clean(String value) {
    return value == null ? "" : value.trim();
  }

  public record SellerSession(String sellerId, String email, String displayName, String token) {
    public JsonObject toJson() {
      return new JsonObject()
        .put("sellerId", sellerId)
        .put("email", email)
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
