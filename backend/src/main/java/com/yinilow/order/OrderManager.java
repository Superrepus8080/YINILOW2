package com.yinilow.order;

import io.vertx.core.json.JsonObject;

public interface OrderManager {
  OrderResult createOrder(JsonObject request, String idempotencyKey);

  record OrderResult(boolean success, int statusCode, JsonObject payload) {
    public static OrderResult success(JsonObject payload) {
      return new OrderResult(true, 201, payload);
    }

    public static OrderResult failure(int statusCode, String errorCode) {
      return new OrderResult(false, statusCode, new JsonObject().put("error", errorCode));
    }
  }
}
