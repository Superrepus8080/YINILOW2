package com.yinilow.payment;

import com.yinilow.db.Database;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;

public class PostgresPaymentService implements PaymentManager {
  private final Database database;
  private final String paystackSecretKey;
  private final String paymentCallbackUrl;
  private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(12)).build();

  public PostgresPaymentService(Database database) {
    this.database = database;
    this.paystackSecretKey = System.getenv("PAYSTACK_SECRET_KEY");
    this.paymentCallbackUrl = System.getenv().getOrDefault("PAYSTACK_CALLBACK_URL", "https://yinilow2.onrender.com");
  }

  @Override
  public PaymentResult initializePayment(JsonObject request, String idempotencyKey) {
    String orderId = request.getString("orderId");
    if (orderId == null || orderId.isBlank()) {
      return PaymentResult.failure(400, "ORDER_ID_REQUIRED");
    }
    String key = idempotencyKey == null || idempotencyKey.isBlank() ? "pay_" + UUID.randomUUID() : idempotencyKey;
    try (Connection connection = database.connection()) {
      return existingAttempt(connection, key)
        .map(PaymentResult::success)
        .orElseGet(() -> createAttempt(connection, orderId, key));
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not initialize payment", exception);
    }
  }

  @Override
  public PaymentResult confirmCallback(JsonObject request) {
    String provider = request.getString("provider", "SANDBOX");
    String reference = request.getString("providerReference");
    if (reference == null || reference.isBlank()) {
      return PaymentResult.failure(400, "PROVIDER_REFERENCE_REQUIRED");
    }

    try (Connection connection = database.connection()) {
      connection.setAutoCommit(false);
      insertCallback(connection, provider, reference, request);
      JsonObject attempt = confirmAttempt(connection, provider, reference);
      if (attempt == null) {
        connection.rollback();
        return PaymentResult.failure(404, "PAYMENT_ATTEMPT_NOT_FOUND");
      }
      connection.commit();
      return PaymentResult.success(attempt);
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not confirm payment", exception);
    }
  }

  private java.util.Optional<JsonObject> existingAttempt(Connection connection, String idempotencyKey) throws SQLException {
    try (var statement = connection.prepareStatement("""
      SELECT attempt.id::text, attempt.order_id::text, attempt.provider, attempt.provider_reference, attempt.amount, attempt.currency, attempt.status
      FROM payments.payment_attempts attempt
      WHERE attempt.idempotency_key = ?
      LIMIT 1
      """)) {
      statement.setString(1, idempotencyKey);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return java.util.Optional.empty();
        }
        return java.util.Optional.of(toPayload(row));
      }
    }
  }

  private PaymentResult createAttempt(Connection connection, String orderId, String idempotencyKey) {
    try (var statement = connection.prepareStatement("""
      INSERT INTO payments.payment_attempts (
        order_id, provider, provider_reference, amount, currency, status, idempotency_key
      )
      SELECT id, ?, ?, total, 'GHS', 'PENDING', ?
      FROM orders.orders
      WHERE id = ?::uuid AND payment_status = 'PENDING'
      RETURNING id::text, order_id::text, provider, provider_reference, amount, currency, status
      """)) {
      String provider = paystackEnabled() ? "PAYSTACK" : "SANDBOX";
      String reference = provider.toLowerCase() + "_" + UUID.randomUUID().toString().replace("-", "");
      statement.setString(1, provider);
      statement.setString(2, reference);
      statement.setString(3, idempotencyKey);
      statement.setString(4, orderId);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return PaymentResult.failure(409, "ORDER_NOT_PAYABLE");
        }
        JsonObject payload = toPayload(row);
        if (paystackEnabled()) {
          return PaymentResult.success(initializePaystack(connection, payload));
        }
        return PaymentResult.success(payload);
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not create payment attempt", exception);
    }
  }

  private JsonObject initializePaystack(Connection connection, JsonObject attempt) {
    JsonObject order = orderForPayment(connection, attempt.getString("orderId"));
    String email = order.getJsonObject("deliveryAddress", new JsonObject()).getString("email", "");
    if (email.isBlank()) {
      email = "customer+" + attempt.getString("providerReference") + "@yinilow.local";
    }
    int amount = new BigDecimal(attempt.getValue("amount").toString())
      .multiply(new BigDecimal("100"))
      .setScale(0, RoundingMode.HALF_UP)
      .intValueExact();
    JsonObject requestBody = new JsonObject()
      .put("email", email)
      .put("amount", amount)
      .put("currency", attempt.getString("currency", "GHS"))
      .put("reference", attempt.getString("providerReference"))
      .put("callback_url", paymentCallbackUrl)
      .put("channels", new io.vertx.core.json.JsonArray().add("card").add("mobile_money").add("bank_transfer"))
      .put("metadata", new JsonObject()
        .put("orderId", attempt.getString("orderId"))
        .put("orderNumber", order.getString("orderNumber")));
    JsonObject response = paystack("POST", "/transaction/initialize", requestBody);
    JsonObject data = response.getJsonObject("data", new JsonObject());
    return attempt
      .put("status", "PENDING")
      .put("authorizationUrl", data.getString("authorization_url", attempt.getString("authorizationUrl")))
      .put("accessCode", data.getString("access_code"));
  }

  private static void insertCallback(Connection connection, String provider, String reference, JsonObject payload) throws SQLException {
    try (var statement = connection.prepareStatement("""
      INSERT INTO payments.provider_callbacks (provider, provider_reference, payload)
      VALUES (?, ?, ?::jsonb)
      """)) {
      statement.setString(1, provider);
      statement.setString(2, reference);
      statement.setString(3, payload.encode());
      statement.executeUpdate();
    }
  }

  private JsonObject confirmAttempt(Connection connection, String provider, String reference) throws SQLException {
    if ("PAYSTACK".equals(provider) && paystackEnabled()) {
      JsonObject verification = paystack("GET", "/transaction/verify/" + reference, null);
      JsonObject data = verification.getJsonObject("data", new JsonObject());
      if (!"success".equals(data.getString("status")) || !paystackAmountMatches(connection, provider, reference, data)) {
        markAttemptFailed(connection, provider, reference);
        return null;
      }
    }
    try (var statement = connection.prepareStatement("""
      UPDATE payments.payment_attempts attempt
      SET status = 'CONFIRMED', updated_at = now()
      WHERE attempt.provider = ?
        AND attempt.provider_reference = ?
        AND attempt.status IN ('STARTED', 'PENDING', 'CONFIRMED')
      RETURNING attempt.id::text, attempt.order_id::text, attempt.provider, attempt.provider_reference, attempt.amount, attempt.currency, attempt.status
      """)) {
      statement.setString(1, provider);
      statement.setString(2, reference);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return null;
        }
        JsonObject payload = toPayload(row).put("paymentStatus", "PAID");
        markOrderPaid(connection, payload.getString("orderId"));
        return payload;
      }
    }
  }

  private static void markOrderPaid(Connection connection, String orderId) throws SQLException {
    try (var statement = connection.prepareStatement("""
      UPDATE orders.orders
      SET payment_status = 'PAID', status = 'PAID'
      WHERE id = ?::uuid
      """)) {
      statement.setString(1, orderId);
      statement.executeUpdate();
    }
  }

  private static void markAttemptFailed(Connection connection, String provider, String reference) throws SQLException {
    try (var statement = connection.prepareStatement("""
      UPDATE payments.payment_attempts
      SET status = 'FAILED', updated_at = now()
      WHERE provider = ? AND provider_reference = ?
      """)) {
      statement.setString(1, provider);
      statement.setString(2, reference);
      statement.executeUpdate();
    }
  }

  private static boolean paystackAmountMatches(Connection connection, String provider, String reference, JsonObject data) throws SQLException {
    Integer paidSubunits = data.getInteger("amount");
    if (paidSubunits == null) {
      return false;
    }
    try (var statement = connection.prepareStatement("""
      SELECT amount
      FROM payments.payment_attempts
      WHERE provider = ? AND provider_reference = ?
      LIMIT 1
      """)) {
      statement.setString(1, provider);
      statement.setString(2, reference);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return false;
        }
        int expectedSubunits = row.getBigDecimal("amount")
          .multiply(new BigDecimal("100"))
          .setScale(0, RoundingMode.HALF_UP)
          .intValueExact();
        return expectedSubunits == paidSubunits;
      }
    }
  }

  private JsonObject orderForPayment(Connection connection, String orderId) {
    try (var statement = connection.prepareStatement("""
      SELECT order_number, delivery_address
      FROM orders.orders
      WHERE id = ?::uuid
      """)) {
      statement.setString(1, orderId);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return new JsonObject();
        }
        return new JsonObject()
          .put("orderNumber", row.getString("order_number"))
          .put("deliveryAddress", new JsonObject(row.getString("delivery_address")));
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not load order for payment", exception);
    }
  }

  private JsonObject paystack(String method, String path, JsonObject body) {
    try {
      HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create("https://api.paystack.co" + path))
        .timeout(Duration.ofSeconds(25))
        .header("Authorization", "Bearer " + paystackSecretKey)
        .header("Content-Type", "application/json");
      if ("POST".equals(method)) {
        builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body.encode()));
      } else {
        builder.GET();
      }
      HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      JsonObject payload = new JsonObject(response.body());
      if (response.statusCode() < 200 || response.statusCode() >= 300 || !payload.getBoolean("status", false)) {
        throw new IllegalStateException("Paystack request failed");
      }
      return payload;
    } catch (IOException exception) {
      throw new IllegalStateException("Could not reach Paystack", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Paystack request interrupted", exception);
    }
  }

  private boolean paystackEnabled() {
    return paystackSecretKey != null && !paystackSecretKey.isBlank();
  }

  private static JsonObject toPayload(ResultSet row) throws SQLException {
    return new JsonObject()
      .put("paymentAttemptId", row.getString("id"))
      .put("orderId", row.getString("order_id"))
      .put("provider", row.getString("provider"))
      .put("providerReference", row.getString("provider_reference"))
      .put("amount", row.getBigDecimal("amount"))
      .put("currency", row.getString("currency"))
      .put("status", row.getString("status"))
      .put("authorizationUrl", "sandbox://pay/" + row.getString("provider_reference"));
  }
}
