package com.yinilow.order;

import com.yinilow.cart.CartService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryOrderService implements OrderManager {
  private final CartService carts;
  private final Map<String, JsonObject> idempotencyResponses = new ConcurrentHashMap<>();
  private final Map<String, JsonObject> ordersByNumber = new ConcurrentHashMap<>();

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
      .put("deliveryAddress", request.getJsonObject("deliveryAddress", new JsonObject()))
      .put("total", quote.getValue("total"))
      .put("currency", quote.getString("currency"));
    if (idempotencyKey != null) {
      idempotencyResponses.put(idempotencyKey, payload);
    }
    ordersByNumber.put(payload.getString("orderNumber").toUpperCase(), payload);
    return OrderResult.success(payload);
  }

  @Override
  public OrderResult getOrder(String orderNumber, String phone) {
    if (orderNumber == null || orderNumber.isBlank() || phone == null || phone.isBlank()) {
      return OrderResult.failure(400, "ORDER_NUMBER_AND_PHONE_REQUIRED");
    }
    JsonObject order = ordersByNumber.get(orderNumber.trim().toUpperCase());
    if (order == null) {
      return OrderResult.failure(404, "ORDER_NOT_FOUND");
    }
    String storedPhone = order.getJsonObject("deliveryAddress", new JsonObject()).getString("phone", "");
    if (!storedPhone.replaceAll("\\s+", "").equals(phone.replaceAll("\\s+", ""))) {
      return OrderResult.failure(404, "ORDER_NOT_FOUND");
    }
    return new OrderResult(true, 200, order.copy());
  }

  @Override
  public JsonArray adminOrders(String sellerId) {
    JsonArray orders = new JsonArray();
    ordersByNumber.values().forEach(order -> orders.add(order.copy()));
    return orders;
  }

  @Override
  public OrderResult updateOrderStatus(String orderId, JsonObject request, String sellerId) {
    for (JsonObject order : ordersByNumber.values()) {
      if (order.getString("orderId", "").equals(orderId)) {
        String paymentStatus = request.getString("paymentStatus");
        String deliveryStatus = request.getString("deliveryStatus");
        String status = request.getString("status");
        if (paymentStatus != null && !paymentStatus.isBlank()) order.put("paymentStatus", paymentStatus);
        if (deliveryStatus != null && !deliveryStatus.isBlank()) order.put("deliveryStatus", deliveryStatus);
        if (status != null && !status.isBlank()) order.put("status", status);
        return new OrderResult(true, 200, order.copy());
      }
    }
    return OrderResult.failure(404, "ORDER_NOT_FOUND");
  }
}
