package com.yinilow.cart;

import com.yinilow.catalog.CatalogReader;
import com.yinilow.catalog.Listing;
import com.yinilow.inventory.Hold;
import com.yinilow.inventory.HoldManager;
import com.yinilow.inventory.HoldService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CartService {
  public static final String DEMO_CART_ID = "00000000-0000-0000-0000-000000000001";

  private final CatalogReader catalog;
  private final HoldManager holds;
  private final String activeCartId = DEMO_CART_ID;
  private final Map<String, JsonObject> cartItems = new LinkedHashMap<>();
  private final Map<String, JsonObject> idempotencyResponses = new ConcurrentHashMap<>();

  public CartService(CatalogReader catalog, HoldManager holds) {
    this.catalog = catalog;
    this.holds = holds;
  }

  public AddToBagResult addToBag(JsonObject request, String idempotencyKey) {
    if (idempotencyKey != null && idempotencyResponses.containsKey(idempotencyKey)) {
      return AddToBagResult.success(idempotencyResponses.get(idempotencyKey));
    }

    String listingId = request.getString("listingId");
    Optional<Listing> maybeListing = catalog.listing(listingId);
    if (maybeListing.isEmpty()) {
      return AddToBagResult.failure(404, "LISTING_NOT_FOUND");
    }

    Listing listing = maybeListing.get();
    if (!listing.available()) {
      return AddToBagResult.failure(409, "ITEM_UNAVAILABLE");
    }

    HoldService.HoldResult holdResult = holds.createHold(listing.itemUnitId(), listing.listingId(), activeCartId);
    if (!holdResult.created()) {
      return AddToBagResult.failure(409, holdResult.errorCode());
    }

    Hold hold = holdResult.hold();
    JsonObject cartItem = new JsonObject()
      .put("cartItemId", "ci_" + UUID.randomUUID())
      .put("cartId", activeCartId)
      .put("listingId", listing.listingId())
      .put("itemUnitId", listing.itemUnitId())
      .put("holdId", hold.holdId())
      .put("holdExpiresAt", hold.expiresAt().toString())
      .put("title", listing.title())
      .put("imageUrl", listing.imageUrl())
      .put("sizeLabel", listing.sizeLabel())
      .put("conditionPublic", listing.conditionPublic())
      .put("price", listing.price())
      .put("currency", listing.currency())
      .put("status", "ACTIVE");
    cartItems.put(cartItem.getString("cartItemId"), cartItem);

    JsonObject response = new JsonObject()
      .put("cartId", activeCartId)
      .put("cartItemId", cartItem.getString("cartItemId"))
      .put("holdId", hold.holdId())
      .put("expiresAt", hold.expiresAt().toString())
      .put("itemStatus", "HELD");
    if (idempotencyKey != null) {
      idempotencyResponses.put(idempotencyKey, response);
    }
    return AddToBagResult.success(response);
  }

  public JsonObject getActiveCart() {
    JsonArray items = new JsonArray();
    cartItems.values().forEach(items::add);
    return new JsonObject()
      .put("cartId", activeCartId)
      .put("status", "ACTIVE")
      .put("items", items);
  }

  public JsonObject quote() {
    JsonArray expiredItems = new JsonArray();
    BigDecimal subtotal = BigDecimal.ZERO;
    for (JsonObject item : cartItems.values()) {
      if (!holds.isActive(item.getString("itemUnitId"))) {
        expiredItems.add(item.getString("cartItemId"));
      } else {
        subtotal = subtotal.add(new BigDecimal(item.getValue("price").toString()));
      }
    }
    BigDecimal serviceFee = subtotal.multiply(new BigDecimal("0.05"));
    BigDecimal deliveryFee = subtotal.signum() > 0 ? new BigDecimal("25.00") : BigDecimal.ZERO;
    return new JsonObject()
      .put("cartId", activeCartId)
      .put("checkoutAllowed", expiredItems.isEmpty() && subtotal.signum() > 0)
      .put("expiredCartItems", expiredItems)
      .put("subtotal", subtotal)
      .put("serviceFee", serviceFee)
      .put("deliveryFee", deliveryFee)
      .put("total", subtotal.add(serviceFee).add(deliveryFee))
      .put("currency", "GHS");
  }

  public record AddToBagResult(boolean success, int statusCode, JsonObject payload) {
    static AddToBagResult success(JsonObject payload) {
      return new AddToBagResult(true, 200, payload);
    }

    static AddToBagResult failure(int statusCode, String errorCode) {
      return new AddToBagResult(false, statusCode, new JsonObject().put("error", errorCode));
    }
  }
}
