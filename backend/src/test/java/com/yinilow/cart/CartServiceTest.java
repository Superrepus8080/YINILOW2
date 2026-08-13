package com.yinilow.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yinilow.catalog.CatalogService;
import com.yinilow.inventory.HoldService;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

class CartServiceTest {
  @Test
  void addToBagCreatesHoldAndIdempotentReplayReturnsSameHold() {
    CartService carts = new CartService(CatalogService.seeded(), new HoldService());
    JsonObject request = new JsonObject().put("listingId", "lst_vintage_polo");

    CartService.AddToBagResult first = carts.addToBag(request, "idem_1");
    CartService.AddToBagResult replay = carts.addToBag(request, "idem_1");

    assertTrue(first.success());
    assertTrue(replay.success());
    assertEquals(first.payload().getString("holdId"), replay.payload().getString("holdId"));
  }

  @Test
  void secondCartCannotHoldSameOneOffItem() {
    CartService carts = new CartService(CatalogService.seeded(), new HoldService());
    JsonObject request = new JsonObject().put("listingId", "lst_vintage_polo");

    CartService.AddToBagResult first = carts.addToBag(request, "idem_1");
    CartService.AddToBagResult second = carts.addToBag(request, "idem_2");

    assertTrue(first.success());
    assertEquals(409, second.statusCode());
    assertEquals("ITEM_HELD", second.payload().getString("error"));
  }
}
