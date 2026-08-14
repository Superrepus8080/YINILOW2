package com.yinilow.order;

import com.yinilow.cart.CartService;
import com.yinilow.db.Database;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PostgresOrderService implements OrderManager {
  private final Database database;
  private final Map<String, JsonObject> idempotencyResponses = new ConcurrentHashMap<>();

  public PostgresOrderService(Database database) {
    this.database = database;
  }

  @Override
  public OrderResult createOrder(JsonObject request, String idempotencyKey) {
    if (idempotencyKey != null && idempotencyResponses.containsKey(idempotencyKey)) {
      return OrderResult.success(idempotencyResponses.get(idempotencyKey));
    }

    try (Connection connection = database.connection()) {
      connection.setAutoCommit(false);
      expireOldHolds(connection);
      JsonArray items = activeCartItems(connection);
      if (items.isEmpty()) {
        connection.rollback();
        return OrderResult.failure(409, "CHECKOUT_NOT_ALLOWED");
      }

      BigDecimal subtotal = subtotal(items);
      BigDecimal serviceFee = subtotal.multiply(new BigDecimal("0.05"));
      BigDecimal deliveryFee = new BigDecimal("25.00");
      BigDecimal total = subtotal.add(serviceFee).add(deliveryFee);
      String orderNumber = "YLO-" + System.currentTimeMillis();
      String orderId = insertOrder(connection, orderNumber, subtotal, serviceFee, deliveryFee, total, request);
      insertOrderItems(connection, orderId, items);
      convertHoldsAndCloseCart(connection, orderId, items);
      connection.commit();

      JsonObject payload = new JsonObject()
        .put("orderId", orderId)
        .put("orderNumber", orderNumber)
        .put("status", "PAYMENT_PENDING")
        .put("paymentStatus", "PENDING")
        .put("deliveryStatus", "NOT_STARTED")
        .put("subtotal", subtotal)
        .put("serviceFee", serviceFee)
        .put("deliveryFee", deliveryFee)
        .put("total", total)
        .put("currency", "GHS")
        .put("items", items);
      if (idempotencyKey != null) {
        idempotencyResponses.put(idempotencyKey, payload);
      }
      return OrderResult.success(payload);
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not create order", exception);
    }
  }

  private static void expireOldHolds(Connection connection) throws SQLException {
    try (var statement = connection.prepareStatement("""
      UPDATE inventory.inventory_holds
      SET status = 'EXPIRED'
      WHERE status = 'ACTIVE' AND expires_at <= now()
      """)) {
      statement.executeUpdate();
    }
  }

  private static JsonArray activeCartItems(Connection connection) throws SQLException {
    try (var statement = connection.prepareStatement("""
      SELECT hold.id::text AS hold_id,
        listing.id::text AS listing_id,
        unit.id::text AS item_unit_id,
        listing.public_code,
        listing.title,
        listing.public_price,
        listing.currency
      FROM inventory.inventory_holds hold
      JOIN catalog.product_listings listing ON listing.id = hold.listing_id
      JOIN catalog.item_units unit ON unit.id = hold.item_unit_id
      WHERE hold.cart_id = ?::uuid
        AND hold.status = 'ACTIVE'
        AND hold.expires_at > now()
      ORDER BY hold.created_at ASC
      """)) {
      statement.setString(1, CartService.DEMO_CART_ID);
      JsonArray items = new JsonArray();
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          items.add(new JsonObject()
            .put("holdId", rows.getString("hold_id"))
            .put("listingUuid", rows.getString("listing_id"))
            .put("itemUnitUuid", rows.getString("item_unit_id"))
            .put("listingId", rows.getString("public_code"))
            .put("title", rows.getString("title"))
            .put("price", rows.getBigDecimal("public_price"))
            .put("currency", rows.getString("currency")));
        }
      }
      return items;
    }
  }

  private static BigDecimal subtotal(JsonArray items) {
    BigDecimal subtotal = BigDecimal.ZERO;
    for (int index = 0; index < items.size(); index++) {
      subtotal = subtotal.add(new BigDecimal(items.getJsonObject(index).getValue("price").toString()));
    }
    return subtotal;
  }

  private static String insertOrder(Connection connection, String orderNumber, BigDecimal subtotal, BigDecimal serviceFee, BigDecimal deliveryFee, BigDecimal total, JsonObject request) throws SQLException {
    try (var statement = connection.prepareStatement("""
      INSERT INTO orders.orders (
        order_number, status, subtotal, service_fee, delivery_fee, total, delivery_address, payment_status, delivery_status
      )
      VALUES (?, 'PAYMENT_PENDING', ?, ?, ?, ?, ?::jsonb, 'PENDING', 'NOT_STARTED')
      RETURNING id::text
      """)) {
      statement.setString(1, orderNumber);
      statement.setBigDecimal(2, subtotal);
      statement.setBigDecimal(3, serviceFee);
      statement.setBigDecimal(4, deliveryFee);
      statement.setBigDecimal(5, total);
      statement.setString(6, request.getJsonObject("deliveryAddress", new JsonObject()).encode());
      try (ResultSet row = statement.executeQuery()) {
        row.next();
        return row.getString(1);
      }
    }
  }

  private static void insertOrderItems(Connection connection, String orderId, JsonArray items) throws SQLException {
    try (var statement = connection.prepareStatement("""
      INSERT INTO orders.order_items (order_id, listing_id, item_unit_id, final_price)
      VALUES (?::uuid, ?::uuid, ?::uuid, ?)
      """)) {
      for (int index = 0; index < items.size(); index++) {
        JsonObject item = items.getJsonObject(index);
        statement.setString(1, orderId);
        statement.setString(2, item.getString("listingUuid"));
        statement.setString(3, item.getString("itemUnitUuid"));
        statement.setBigDecimal(4, new BigDecimal(item.getValue("price").toString()));
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void convertHoldsAndCloseCart(Connection connection, String orderId, JsonArray items) throws SQLException {
    try (var holdStatement = connection.prepareStatement("""
        UPDATE inventory.inventory_holds
        SET status = 'CONVERTED'
        WHERE id = ?::uuid AND status = 'ACTIVE'
        """);
         var unitStatement = connection.prepareStatement("""
        UPDATE catalog.item_units
        SET status = 'SOLD', updated_at = now()
        WHERE id = ?::uuid
        """)) {
      for (int index = 0; index < items.size(); index++) {
        JsonObject item = items.getJsonObject(index);
        holdStatement.setString(1, item.getString("holdId"));
        holdStatement.addBatch();
        unitStatement.setString(1, item.getString("itemUnitUuid"));
        unitStatement.addBatch();
      }
      holdStatement.executeBatch();
      unitStatement.executeBatch();
    }

    try (var cartItems = connection.prepareStatement("UPDATE cart.cart_items SET status = 'ORDERED' WHERE cart_id = ?::uuid");
         var cart = connection.prepareStatement("UPDATE cart.carts SET status = 'ORDERED', updated_at = now() WHERE id = ?::uuid")) {
      cartItems.setString(1, CartService.DEMO_CART_ID);
      cartItems.executeUpdate();
      cart.setString(1, CartService.DEMO_CART_ID);
      cart.executeUpdate();
    }
  }
}
