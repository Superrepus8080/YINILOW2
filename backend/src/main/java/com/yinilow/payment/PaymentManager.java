package com.yinilow.payment;

import io.vertx.core.json.JsonObject;

public interface PaymentManager {
  PaymentResult initializePayment(JsonObject request, String idempotencyKey);

  PaymentResult confirmCallback(JsonObject request);

  record PaymentResult(boolean success, int statusCode, JsonObject payload) {
    public static PaymentResult success(JsonObject payload) {
      return new PaymentResult(true, 200, payload);
    }

    public static PaymentResult failure(int statusCode, String errorCode) {
      return new PaymentResult(false, statusCode, new JsonObject().put("error", errorCode));
    }
  }
}
