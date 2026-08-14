package com.yinilow.api;

import com.yinilow.auth.SellerAuthService;
import com.yinilow.catalog.CatalogAdmin;
import com.yinilow.catalog.CatalogReader;
import com.yinilow.catalog.CatalogService;
import com.yinilow.catalog.MemoryCatalogAdmin;
import com.yinilow.catalog.PostgresCatalogAdmin;
import com.yinilow.catalog.PostgresCatalogService;
import com.yinilow.cart.CartService;
import com.yinilow.db.Database;
import com.yinilow.db.DatabaseBootstrap;
import com.yinilow.db.DatabaseConfig;
import com.yinilow.inventory.HoldManager;
import com.yinilow.inventory.HoldService;
import com.yinilow.inventory.PostgresHoldService;
import com.yinilow.order.MemoryOrderService;
import com.yinilow.order.OrderManager;
import com.yinilow.order.PostgresOrderService;
import com.yinilow.payment.MemoryPaymentService;
import com.yinilow.payment.PaymentManager;
import com.yinilow.payment.PostgresPaymentService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class MainVerticle extends AbstractVerticle {
  private volatile CatalogReader catalog;
  private volatile CatalogAdmin catalogAdmin;
  private volatile HoldManager holds;
  private volatile CartService carts;
  private volatile OrderManager orders;
  private volatile PaymentManager payments;
  private final SellerAuthService sellerAuth = new SellerAuthService();
  private volatile String dataMode;

  @Override
  public void start(Promise<Void> startPromise) {
    initializeMemoryServices("memory");
    initializePostgresInBackground();

    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create()
      .addOrigin("https://yinilow2.onrender.com")
      .addOriginWithRegex("https://.*\\.onrender\\.com")
      .addOriginWithRegex("http://localhost:[0-9]+")
      .addOriginWithRegex("http://127\\.0\\.0\\.1:[0-9]+")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.PATCH)
      .allowedMethod(HttpMethod.DELETE)
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedHeader("authorization")
      .allowedHeader("content-type")
      .allowedHeader("x-idempotency-key"));
    router.route().handler(BodyHandler.create());

    router.get("/health").handler(ctx -> ctx.json(new JsonObject()
      .put("status", "ok")
      .put("service", "yinilow-clothing-os-api")
      .put("dataMode", dataMode)));

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
    router.post("/api/v1/seller/sessions").handler(ctx -> {
      JsonObject body = ctx.body().asJsonObject();
      SellerAuthService.AuthResult result = sellerAuth.login(body == null ? new JsonObject() : body);
      ctx.response().setStatusCode(result.statusCode()).putHeader("content-type", "application/json").end(result.payload().encode());
    });
    router.post("/api/v1/seller/accounts").handler(ctx -> {
      JsonObject body = ctx.body().asJsonObject();
      SellerAuthService.AuthResult result = sellerAuth.register(body == null ? new JsonObject() : body);
      ctx.response().setStatusCode(result.statusCode()).putHeader("content-type", "application/json").end(result.payload().encode());
    });
    router.delete("/api/v1/seller/session").handler(ctx -> {
      sellerAuth.logout(ctx.request().getHeader("authorization"));
      ctx.response().setStatusCode(204).end();
    });
    router.post("/api/v1/admin/catalog/listings").handler(ctx -> {
      if (!sellerAuth.authenticate(ctx.request().getHeader("authorization")).isPresent()) {
        unauthorized(ctx);
        return;
      }
      JsonObject body = ctx.body().asJsonObject();
      CatalogAdmin.CreateListingResult result = catalogAdmin.createListing(body == null ? new JsonObject() : body);
      ctx.response().setStatusCode(result.statusCode()).putHeader("content-type", "application/json").end(result.payload().encode());
    });
    router.get("/api/v1/admin/catalog/listings").handler(ctx -> {
      if (!sellerAuth.authenticate(ctx.request().getHeader("authorization")).isPresent()) {
        unauthorized(ctx);
        return;
      }
      ctx.json(catalogAdmin.adminListings());
    });
    router.patch("/api/v1/admin/catalog/listings/:id/visibility").handler(ctx -> {
      if (!sellerAuth.authenticate(ctx.request().getHeader("authorization")).isPresent()) {
        unauthorized(ctx);
        return;
      }
      JsonObject body = ctx.body().asJsonObject();
      CatalogAdmin.CreateListingResult result = catalogAdmin.updateVisibility(ctx.pathParam("id"), body == null ? new JsonObject() : body);
      ctx.response().setStatusCode(result.statusCode()).putHeader("content-type", "application/json").end(result.payload().encode());
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
    router.post("/api/v1/checkout/orders").handler(ctx -> {
      JsonObject body = ctx.body().asJsonObject();
      String idempotencyKey = ctx.request().getHeader("x-idempotency-key");
      OrderManager.OrderResult result = orders.createOrder(body == null ? new JsonObject() : body, idempotencyKey);
      ctx.response().setStatusCode(result.statusCode()).putHeader("content-type", "application/json").end(result.payload().encode());
    });
    router.post("/api/v1/payments/initialize").handler(ctx -> {
      JsonObject body = ctx.body().asJsonObject();
      String idempotencyKey = ctx.request().getHeader("x-idempotency-key");
      PaymentManager.PaymentResult result = payments.initializePayment(body == null ? new JsonObject() : body, idempotencyKey);
      ctx.response().setStatusCode(result.statusCode()).putHeader("content-type", "application/json").end(result.payload().encode());
    });
    router.post("/api/v1/payments/callbacks/sandbox").handler(ctx -> {
      JsonObject body = ctx.body().asJsonObject();
      PaymentManager.PaymentResult result = payments.confirmCallback(body == null ? new JsonObject() : body);
      ctx.response().setStatusCode(result.statusCode()).putHeader("content-type", "application/json").end(result.payload().encode());
    });

    int port = config().getInteger("http.port", Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")));
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port)
      .onSuccess(server -> startPromise.complete())
      .onFailure(startPromise::fail);
  }

  private void initializeMemoryServices(String mode) {
    catalog = CatalogService.seeded();
    catalogAdmin = new MemoryCatalogAdmin();
    holds = new HoldService();
    carts = new CartService(catalog, holds);
    orders = new MemoryOrderService(carts);
    payments = new MemoryPaymentService();
    dataMode = mode;
  }

  private void initializePostgresInBackground() {
    DatabaseConfig config = DatabaseConfig.fromEnvironment();
    if (config == null) {
      return;
    }

    dataMode = "postgres_starting";
    vertx.executeBlocking(() -> {
      Database database = new Database(config);
      new DatabaseBootstrap(database).migrateAndSeed();
      sellerAuth.useDatabase(database);
      CatalogReader postgresCatalog = new PostgresCatalogService(database);
      CatalogAdmin postgresCatalogAdmin = new PostgresCatalogAdmin(database);
      HoldManager postgresHolds = new PostgresHoldService(database);
      CartService postgresCarts = new CartService(postgresCatalog, postgresHolds);
      return new DataServices(postgresCatalog, postgresCatalogAdmin, postgresHolds, postgresCarts, new PostgresOrderService(database), new PostgresPaymentService(database));
    }).onSuccess(services -> {
      catalog = services.catalog();
      catalogAdmin = services.catalogAdmin();
      holds = services.holds();
      carts = services.carts();
      orders = services.orders();
      payments = services.payments();
      dataMode = "postgres";
    }).onFailure(error -> {
      error.printStackTrace();
      initializeMemoryServices("memory_database_unavailable");
    });
  }

  private record DataServices(CatalogReader catalog, CatalogAdmin catalogAdmin, HoldManager holds, CartService carts, OrderManager orders, PaymentManager payments) {
  }

  private static void notFound(io.vertx.ext.web.RoutingContext ctx, String code) {
    ctx.response().setStatusCode(404).putHeader("content-type", "application/json").end(new JsonObject()
      .put("error", code)
      .encode());
  }

  private static void unauthorized(io.vertx.ext.web.RoutingContext ctx) {
    ctx.response().setStatusCode(401).putHeader("content-type", "application/json").end(new JsonObject()
      .put("error", "SELLER_SESSION_REQUIRED")
      .encode());
  }
}
