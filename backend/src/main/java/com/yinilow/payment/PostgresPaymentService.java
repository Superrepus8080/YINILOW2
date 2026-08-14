package com.yinilow.payment;

import com.yinilow.db.Database;
import io.vertx.core.json.JsonObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PostgresPaymentService implements PaymentManager {
  private final Database database;

  public PostgresPaymentService(Database database) {
    this.database = database;
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
      SELECT id, 'SANDBOX', ?, total, 'GHS', 'PENDING', ?
      FROM orders.orders
      WHERE id = ?::uuid AND payment_status = 'PENDING'
      RETURNING id::text, order_id::text, provider, provider_reference, amount, currency, status
      """)) {
      String reference = "sandbox_" + UUID.randomUUID();
      statement.setString(1, reference);
      statement.setString(2, idempotencyKey);
      statement.setString(3, orderId);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return PaymentResult.failure(409, "ORDER_NOT_PAYABLE");
        }
        return PaymentResult.success(toPayload(row));
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not create payment attempt", exception);
    }
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

  private static JsonObject confirmAttempt(Connection connection, String provider, String reference) throws SQLException {
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
