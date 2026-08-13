package com.yinilow.api;

import com.yinilow.catalog.CatalogService;
import com.yinilow.cart.CartService;
import com.yinilow.inventory.HoldService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class MainVerticle extends AbstractVerticle {
  private final CatalogService catalog = CatalogService.seeded();
  private final HoldService holds = new HoldService();
  private final CartService carts = new CartService(catalog, holds);

  @Override
  public void start(Promise<Void> startPromise) {
    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create().allowedMethod(io.vertx.core.http.HttpMethod.GET)
      .allowedMethod(io.vertx.core.http.HttpMethod.POST)
      .allowedMethod(io.vertx.core.http.HttpMethod.PATCH)
      .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
      .allowedHeader("content-type")
      .allowedHeader("x-idempotency-key"));
    router.route().handler(BodyHandler.create());

    router.get("/health").handler(ctx -> ctx.json(new JsonObject()
      .put("status", "ok")
      .put("service", "yinilow-clothing-os-api")));

    router.get("/api/v1/config/feature-flags").handler(ctx -> ctx.json(new JsonObject()
      .put("digPile.enabled", true)
      .put("digPile.live.enabled", false)
      .put("digPile.webrtc.enabled", false)
      .put("luckyPull.enabled", false)
      .put("savedPile.enabled", true)
      .put("grab.enabled", false)
      .put("stockDrop.enabled", false)
      .put("cod.enabled", false)
      .put("premiumVideo.required", true)
      .put("partnerIdentity.enabled", false)));

    router.get("/api/v1/catalog/categories").handler(ctx -> ctx.json(catalog.categories()));
    router.get("/api/v1/catalog/listings").handler(ctx -> ctx.json(catalog.publicListingCards()));
    router.get("/api/v1/catalog/listings/:id").handler(ctx -> {
      String listingId = ctx.pathParam("id");
      catalog.publicListingDetail(listingId)
        .ifPresentOrElse(ctx::json, () -> notFound(ctx, "LISTING_NOT_FOUND"));
    });

    router.get("/api/v1/cart").handler(ctx -> ctx.json(carts.getActiveCart()));
    router.post("/api/v1/cart/items").handler(ctx -> {
      JsonObject body = ctx.body().asJsonObject();
      String idempotencyKey = ctx.request().getHeader("x-idempotency-key");
      CartService.AddToBagResult result = carts.addToBag(body, idempotencyKey);
      if (result.success()) {
        ctx.response().setStatusCode(201).putHeader("content-type", "application/json").end(result.payload().encode());
      } else {
        ctx.response().setStatusCode(result.statusCode()).putHeader("content-type", "application/json").end(result.payload().encode());
      }
    });

    router.post("/api/v1/checkout/quote").handler(ctx -> {
      JsonObject quote = carts.quote();
      ctx.response().setStatusCode(quote.getBoolean("checkoutAllowed") ? 200 : 409).putHeader("content-type", "application/json").end(quote.encode());
    });

    int port = config().getInteger("http.port", Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")));
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port)
      .onSuccess(server -> startPromise.complete())
      .onFailure(startPromise::fail);
  }

  private static void notFound(io.vertx.ext.web.RoutingContext ctx, String code) {
    ctx.response().setStatusCode(404).putHeader("content-type", "application/json").end(new JsonObject()
      .put("error", code)
      .encode());
  }
}
