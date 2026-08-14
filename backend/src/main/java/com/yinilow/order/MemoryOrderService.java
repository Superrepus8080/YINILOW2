package com.yinilow.order;

import com.yinilow.cart.CartService;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryOrderService implements OrderManager {
  private final CartService carts;
  private final Map<String, JsonObject> idempotencyResponses = new ConcurrentHashMap<>();

  public MemoryOrderService(CartService carts) {
    this.carts = carts;
  }

  @Override
  public OrderResult createOrder(JsonObject request, String idempotencyKey) {
    if (idempotencyKey != null && idempotencyResponses.containsKey(idempotencyKey)) {
      return OrderResult.success(idempotencyResponses.get(idempotencyKey));
    }
    JsonObject quote = carts.quote();
    if (!quote.getBoolean("checkoutAllowed")) {
      return OrderResult.failure(409, "CHECKOUT_NOT_ALLOWED");
    }
    JsonObject payload = new JsonObject()
      .put("orderId", "ord_" + UUID.randomUUID())
      .put("orderNumber", "YLO-" + System.currentTimeMillis())
      .put("status", "PAYMENT_PENDING")
      .put("paymentStatus", "PENDING")
      .put("deliveryStatus", "NOT_STARTED")
      .put("total", quote.getValue("total"))
      .put("currency", quote.getString("currency"));
    if (idempotencyKey != null) {
      idempotencyResponses.put(idempotencyKey, payload);
    }
    return OrderResult.success(payload);
  }
}
