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
        .put("deliveryAddress", request.getJsonObject("deliveryAddress", new JsonObject()))
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

  @Override
  public OrderResult getOrder(String orderNumber, String phone) {
    if (orderNumber == null || orderNumber.isBlank() || phone == null || phone.isBlank()) {
      return OrderResult.failure(400, "ORDER_NUMBER_AND_PHONE_REQUIRED");
    }

    try (Connection connection = database.connection()) {
      JsonObject order = findOrder(connection, orderNumber.trim(), phone.trim());
      if (order == null) {
        return OrderResult.failure(404, "ORDER_NOT_FOUND");
      }
      order.put("items", orderItems(connection, order.getString("orderId")));
      return new OrderResult(true, 200, order);
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not get order", exception);
    }
  }

  @Override
  public JsonArray adminOrders(String sellerId) {
    try (Connection connection = database.connection();
         var statement = connection.prepareStatement("""
      SELECT DISTINCT ord.id::text,
        ord.order_number,
        ord.status,
        ord.payment_status,
        ord.delivery_status,
        ord.total,
        ord.delivery_address,
        ord.created_at
      FROM orders.orders ord
      JOIN orders.order_items item ON item.order_id = ord.id
      JOIN catalog.product_listings listing ON listing.id = item.listing_id
      WHERE listing.seller_account_id = ?::uuid
      ORDER BY ord.created_at DESC
      LIMIT 100
      """)) {
      statement.setString(1, sellerId);
      JsonArray orders = new JsonArray();
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          String orderId = rows.getString("id");
          orders.add(new JsonObject()
            .put("orderId", orderId)
            .put("orderNumber", rows.getString("order_number"))
            .put("status", rows.getString("status"))
            .put("paymentStatus", rows.getString("payment_status"))
            .put("deliveryStatus", rows.getString("delivery_status"))
            .put("total", rows.getBigDecimal("total"))
            .put("currency", "GHS")
            .put("deliveryAddress", new JsonObject(rows.getString("delivery_address")))
            .put("createdAt", rows.getString("created_at"))
            .put("items", orderItems(connection, orderId)));
        }
      }
      return orders;
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not load seller orders", exception);
    }
  }

  @Override
  public OrderResult updateOrderStatus(String orderId, JsonObject request, String sellerId) {
    String status = normalizeStatus(request.getString("status"));
    String paymentStatus = normalizePaymentStatus(request.getString("paymentStatus"));
    String deliveryStatus = normalizeDeliveryStatus(request.getString("deliveryStatus"));
    if (status == null && paymentStatus == null && deliveryStatus == null) {
      return OrderResult.failure(400, "ORDER_STATUS_REQUIRED");
    }

    try (Connection connection = database.connection()) {
      if (!sellerOwnsOrder(connection, orderId, sellerId)) {
        return OrderResult.failure(404, "ORDER_NOT_FOUND");
      }
      try (var statement = connection.prepareStatement("""
        UPDATE orders.orders
        SET status = COALESCE(?, status),
          payment_status = COALESCE(?, payment_status),
          delivery_status = COALESCE(?, delivery_status)
        WHERE id = ?::uuid
        RETURNING id::text, order_number, status, payment_status, delivery_status, subtotal, service_fee, delivery_fee, total, delivery_address, created_at
        """)) {
        statement.setString(1, status);
        statement.setString(2, paymentStatus);
        statement.setString(3, deliveryStatus);
        statement.setString(4, orderId);
        try (ResultSet row = statement.executeQuery()) {
          if (!row.next()) {
            return OrderResult.failure(404, "ORDER_NOT_FOUND");
          }
          JsonObject order = new JsonObject()
            .put("orderId", row.getString("id"))
            .put("orderNumber", row.getString("order_number"))
            .put("status", row.getString("status"))
            .put("paymentStatus", row.getString("payment_status"))
            .put("deliveryStatus", row.getString("delivery_status"))
            .put("subtotal", row.getBigDecimal("subtotal"))
            .put("serviceFee", row.getBigDecimal("service_fee"))
            .put("deliveryFee", row.getBigDecimal("delivery_fee"))
            .put("total", row.getBigDecimal("total"))
            .put("currency", "GHS")
            .put("deliveryAddress", new JsonObject(row.getString("delivery_address")))
            .put("createdAt", row.getString("created_at"))
            .put("items", orderItems(connection, orderId));
          return new OrderResult(true, 200, order);
        }
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not update order status", exception);
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

  private static JsonObject findOrder(Connection connection, String orderNumber, String phone) throws SQLException {
    try (var statement = connection.prepareStatement("""
      SELECT id::text, order_number, status, payment_status, delivery_status, subtotal, service_fee, delivery_fee, total, delivery_address, created_at
      FROM orders.orders
      WHERE upper(order_number) = upper(?)
        AND regexp_replace(coalesce(delivery_address->>'phone', ''), '\\s+', '', 'g') = regexp_replace(?, '\\s+', '', 'g')
      LIMIT 1
      """)) {
      statement.setString(1, orderNumber);
      statement.setString(2, phone);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return null;
        }
        return new JsonObject()
          .put("orderId", row.getString("id"))
          .put("orderNumber", row.getString("order_number"))
          .put("status", row.getString("status"))
          .put("paymentStatus", row.getString("payment_status"))
          .put("deliveryStatus", row.getString("delivery_status"))
          .put("subtotal", row.getBigDecimal("subtotal"))
          .put("serviceFee", row.getBigDecimal("service_fee"))
          .put("deliveryFee", row.getBigDecimal("delivery_fee"))
          .put("total", row.getBigDecimal("total"))
          .put("currency", "GHS")
          .put("deliveryAddress", new JsonObject(row.getString("delivery_address")))
          .put("createdAt", row.getString("created_at"));
      }
    }
  }

  private static JsonArray orderItems(Connection connection, String orderId) throws SQLException {
    try (var statement = connection.prepareStatement("""
      SELECT listing.public_code, listing.title, listing.public_price, listing.currency, image.url AS image_url, unit.size_label
      FROM orders.order_items item
      JOIN catalog.product_listings listing ON listing.id = item.listing_id
      JOIN catalog.item_units unit ON unit.id = item.item_unit_id
      LEFT JOIN LATERAL (
        SELECT url
        FROM catalog.item_media
        WHERE item_unit_id = unit.id AND approved = true
        ORDER BY purpose = 'PRIMARY' DESC, created_at ASC
        LIMIT 1
      ) image ON true
      WHERE item.order_id = ?::uuid
      ORDER BY listing.title ASC
      """)) {
      statement.setString(1, orderId);
      JsonArray items = new JsonArray();
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          items.add(new JsonObject()
            .put("listingId", rows.getString("public_code"))
            .put("title", rows.getString("title"))
            .put("price", rows.getBigDecimal("public_price"))
            .put("currency", rows.getString("currency"))
            .put("imageUrl", rows.getString("image_url"))
            .put("sizeLabel", rows.getString("size_label")));
        }
      }
      return items;
    }
  }

  private static boolean sellerOwnsOrder(Connection connection, String orderId, String sellerId) throws SQLException {
    try (var statement = connection.prepareStatement("""
      SELECT 1
      FROM orders.order_items item
      JOIN catalog.product_listings listing ON listing.id = item.listing_id
      WHERE item.order_id = ?::uuid AND listing.seller_account_id = ?::uuid
      LIMIT 1
      """)) {
      statement.setString(1, orderId);
      statement.setString(2, sellerId);
      try (ResultSet row = statement.executeQuery()) {
        return row.next();
      }
    }
  }

  private static String normalizeStatus(String value) {
    if (value == null || value.isBlank()) return null;
    return switch (value) {
      case "PAYMENT_PENDING", "PAID", "PACKED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED" -> value;
      default -> null;
    };
  }

  private static String normalizePaymentStatus(String value) {
    if (value == null || value.isBlank()) return null;
    return switch (value) {
      case "PENDING", "PAID", "FAILED", "REFUNDED" -> value;
      default -> null;
    };
  }

  private static String normalizeDeliveryStatus(String value) {
    if (value == null || value.isBlank()) return null;
    return switch (value) {
      case "NOT_STARTED", "PACKED", "OUT_FOR_DELIVERY", "DELIVERED", "RETURNED" -> value;
      default -> null;
    };
  }
}
