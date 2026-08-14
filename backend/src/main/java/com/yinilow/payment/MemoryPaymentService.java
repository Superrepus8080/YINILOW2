package com.yinilow.payment;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryPaymentService implements PaymentManager {
  private final Map<String, JsonObject> attempts = new ConcurrentHashMap<>();

  @Override
  public PaymentResult initializePayment(JsonObject request, String idempotencyKey) {
    String orderId = request.getString("orderId");
    if (orderId == null || orderId.isBlank()) {
      return PaymentResult.failure(400, "ORDER_ID_REQUIRED");
    }
    String reference = idempotencyKey != null && attempts.containsKey(idempotencyKey)
      ? attempts.get(idempotencyKey).getString("providerReference")
      : "sandbox_" + UUID.randomUUID();
    JsonObject payload = new JsonObject()
      .put("paymentAttemptId", "pay_" + UUID.randomUUID())
      .put("orderId", orderId)
      .put("provider", "SANDBOX")
      .put("providerReference", reference)
      .put("status", "PENDING")
      .put("authorizationUrl", "sandbox://pay/" + reference);
    if (idempotencyKey != null) {
      attempts.put(idempotencyKey, payload);
    }
    return PaymentResult.success(payload);
  }

  @Override
  public PaymentResult confirmCallback(JsonObject request) {
    return PaymentResult.success(new JsonObject()
      .put("provider", request.getString("provider", "SANDBOX"))
      .put("providerReference", request.getString("providerReference"))
      .put("status", "CONFIRMED")
      .put("paymentStatus", "PAID"));
  }
}
