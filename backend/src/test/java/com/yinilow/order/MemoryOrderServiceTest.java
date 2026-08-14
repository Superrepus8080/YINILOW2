package com.yinilow.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yinilow.cart.CartService;
import com.yinilow.catalog.CatalogService;
import com.yinilow.inventory.HoldService;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

class MemoryOrderServiceTest {
  @Test
  void createsOrderWhenCartHasActiveHold() {
    CartService carts = new CartService(CatalogService.seeded(), new HoldService());
    carts.addToBag(new JsonObject().put("listingId", "lst_vintage_polo"), "idem_1");
    MemoryOrderService orders = new MemoryOrderService(carts);

    OrderManager.OrderResult result = orders.createOrder(new JsonObject()
      .put("deliveryAddress", new JsonObject().put("phone", "024 000 0000")), "order_1");

    assertTrue(result.success());
    assertEquals(201, result.statusCode());
    assertEquals("PAYMENT_PENDING", result.payload().getString("status"));
  }

  @Test
  void tracksOrderByOrderNumberAndPhone() {
    CartService carts = new CartService(CatalogService.seeded(), new HoldService());
    carts.addToBag(new JsonObject().put("listingId", "lst_vintage_polo"), "idem_1");
    MemoryOrderService orders = new MemoryOrderService(carts);
    JsonObject deliveryAddress = new JsonObject().put("phone", "024 000 0000");
    JsonObject created = orders.createOrder(new JsonObject().put("deliveryAddress", deliveryAddress), "order_1").payload();

    OrderManager.OrderResult result = orders.getOrder(created.getString("orderNumber"), "0240000000");

    assertTrue(result.success());
    assertEquals(200, result.statusCode());
    assertEquals(created.getString("orderId"), result.payload().getString("orderId"));
  }

  @Test
  void blocksOrderWhenCartIsEmpty() {
    CartService carts = new CartService(CatalogService.seeded(), new HoldService());
    MemoryOrderService orders = new MemoryOrderService(carts);

    OrderManager.OrderResult result = orders.createOrder(new JsonObject(), "order_1");

    assertEquals(409, result.statusCode());
    assertEquals("CHECKOUT_NOT_ALLOWED", result.payload().getString("error"));
  }
}
