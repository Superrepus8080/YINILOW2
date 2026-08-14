package com.yinilow.catalog;

import com.yinilow.db.Database;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Locale;

public class PostgresCatalogAdmin implements CatalogAdmin {
  private static final int MAX_IMAGE_URL_LENGTH = 2_100_000;
  private final Database database;

  public PostgresCatalogAdmin(Database database) {
    this.database = database;
  }

  @Override
  public JsonArray adminListings(String sellerId) {
    String sql = """
      SELECT
        listing.public_code,
        listing.title,
        listing.public_price,
        listing.currency,
        listing.visibility,
        listing.published_at,
        category.name AS category,
        unit.unit_code,
        unit.status AS item_status,
        unit.size_label,
        unit.condition_public,
        listing_unit.status AS listing_unit_status,
        COALESCE(media.url, '/assets/prod-tee.jpg') AS image_url,
        EXISTS (
          SELECT 1
          FROM inventory.inventory_holds hold
          WHERE hold.item_unit_id = unit.id
            AND hold.status = 'ACTIVE'
            AND hold.expires_at > now()
        ) AS has_active_hold
      FROM catalog.product_listings listing
      JOIN catalog.categories category ON category.id = listing.category_id
      JOIN catalog.listing_units listing_unit ON listing_unit.listing_id = listing.id
      JOIN catalog.item_units unit ON unit.id = listing_unit.item_unit_id
      LEFT JOIN LATERAL (
        SELECT url
        FROM catalog.item_media
        WHERE item_unit_id = unit.id AND approved = true
        ORDER BY purpose = 'PRIMARY' DESC, created_at ASC
        LIMIT 1
      ) media ON true
      WHERE listing.seller_account_id = ?::uuid
      ORDER BY listing.created_at DESC
      """;

    try (Connection connection = database.connection();
         var statement = connection.prepareStatement(sql)) {
      statement.setString(1, sellerId);
      try (ResultSet rows = statement.executeQuery()) {
      JsonArray listings = new JsonArray();
      while (rows.next()) {
        String itemStatus = rows.getString("item_status");
        boolean publicAndListed = "PUBLIC".equals(rows.getString("visibility")) && "LISTED".equals(itemStatus);
        listings.add(new JsonObject()
          .put("listingId", rows.getString("public_code"))
          .put("title", rows.getString("title"))
          .put("category", rows.getString("category"))
          .put("price", rows.getBigDecimal("public_price"))
          .put("currency", rows.getString("currency"))
          .put("visibility", rows.getString("visibility"))
          .put("itemUnitId", rows.getString("unit_code"))
          .put("itemStatus", itemStatus)
          .put("listingUnitStatus", rows.getString("listing_unit_status"))
          .put("sizeLabel", rows.getString("size_label"))
          .put("conditionPublic", rows.getString("condition_public"))
          .put("imageUrl", rows.getString("image_url"))
          .put("hasActiveHold", rows.getBoolean("has_active_hold"))
          .put("availabilityState", publicAndListed && !rows.getBoolean("has_active_hold") ? "AVAILABLE" : "UNAVAILABLE")
          .put("publishedAt", rows.getString("published_at")));
      }
      return listings;
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not load admin listings", exception);
    }
  }

  @Override
  public CreateListingResult createListing(JsonObject request, String sellerId) {
    String title = clean(request.getString("title"));
    String category = clean(request.getString("category", "New Drop"));
    BigDecimal price = amount(request.getValue("price"));
    if (title.isBlank()) {
      return CreateListingResult.failure(400, "TITLE_REQUIRED");
    }
    if (price.compareTo(BigDecimal.ZERO) <= 0) {
      return CreateListingResult.failure(400, "PRICE_REQUIRED");
    }
    String imageUrl = request.getString("imageUrl", "/assets/prod-tee.jpg");
    if (!validImageUrl(imageUrl)) {
      return CreateListingResult.failure(400, "IMAGE_NOT_ALLOWED");
    }

    try (Connection connection = database.connection()) {
      connection.setAutoCommit(false);
      String categoryId = ensureCategory(connection, category);
      String unitCode = uniqueCode("unit", title);
      String listingCode = uniqueCode("lst", title);
      String itemUnitId = createItemUnit(connection, request, unitCode);
      String listingId = createProductListing(connection, request, listingCode, title, slug(listingCode), categoryId, price, sellerId);
      linkListingUnit(connection, listingId, itemUnitId);
      createImage(connection, itemUnitId, imageUrl);
      createMeasurements(connection, itemUnitId, request.getJsonObject("measurements", new JsonObject()));
      connection.commit();
      return CreateListingResult.created(new JsonObject()
        .put("listingId", listingCode)
        .put("itemUnitId", unitCode)
        .put("title", title)
        .put("category", category)
        .put("price", price)
        .put("currency", "GHS")
        .put("availabilityState", "AVAILABLE"));
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not create catalog listing", exception);
    }
  }

  @Override
  public CreateListingResult updateVisibility(String listingId, JsonObject request, String sellerId) {
    String visibility = clean(request.getString("visibility", "PUBLIC")).toUpperCase(Locale.ROOT);
    if (!visibility.equals("PUBLIC") && !visibility.equals("HIDDEN")) {
      return CreateListingResult.failure(400, "VISIBILITY_NOT_ALLOWED");
    }

    try (Connection connection = database.connection();
         var statement = connection.prepareStatement("""
           UPDATE catalog.product_listings
           SET visibility = ?, published_at = CASE WHEN ? = 'PUBLIC' THEN COALESCE(published_at, now()) ELSE published_at END, updated_at = now()
           WHERE public_code = ? AND seller_account_id = ?::uuid
           RETURNING public_code, title, visibility
           """)) {
      statement.setString(1, visibility);
      statement.setString(2, visibility);
      statement.setString(3, listingId);
      statement.setString(4, sellerId);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return CreateListingResult.failure(404, "LISTING_NOT_FOUND");
        }
        return CreateListingResult.created(new JsonObject()
          .put("listingId", row.getString("public_code"))
          .put("title", row.getString("title"))
          .put("visibility", row.getString("visibility")));
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not update listing visibility", exception);
    }
  }

  private static String ensureCategory(Connection connection, String name) throws SQLException {
    String safeName = name.isBlank() ? "New Drop" : name;
    try (var statement = connection.prepareStatement("""
      INSERT INTO catalog.categories (name, slug)
      VALUES (?, ?)
      ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name
      RETURNING id::text
      """)) {
      statement.setString(1, safeName);
      statement.setString(2, slug(safeName));
      try (ResultSet row = statement.executeQuery()) {
        row.next();
        return row.getString("id");
      }
    }
  }

  private static String createItemUnit(Connection connection, JsonObject request, String unitCode) throws SQLException {
    try (var statement = connection.prepareStatement("""
      INSERT INTO catalog.item_units (
        stock_type, unit_code, source_type, economic_owner_type, status, size_label, color_label, condition_public
      )
      VALUES ('THRIFT_ONE_OFF', ?, 'SELLER', 'SELLER', 'LISTED', ?, ?, ?)
      RETURNING id::text
      """)) {
      statement.setString(1, unitCode);
      statement.setString(2, clean(request.getString("sizeLabel", "One size")));
      statement.setString(3, clean(request.getString("colorLabel", "")));
      statement.setString(4, clean(request.getString("conditionPublic", "GOOD")));
      try (ResultSet row = statement.executeQuery()) {
        row.next();
        return row.getString("id");
      }
    }
  }

  private static String createProductListing(
    Connection connection,
    JsonObject request,
    String listingCode,
    String title,
    String slug,
    String categoryId,
    BigDecimal price,
    String sellerId
  ) throws SQLException {
    try (var statement = connection.prepareStatement("""
      INSERT INTO catalog.product_listings (
        public_code, title, slug, description, category_id, stock_type, pile_eligible, restockable,
        return_policy_type, visibility, public_price, currency, published_at, seller_account_id
      )
      VALUES (?, ?, ?, ?, ?::uuid, 'THRIFT_ONE_OFF', ?, false, 'THRIFT_LIMITED_RETURN', 'PUBLIC', ?, 'GHS', now(), ?::uuid)
      RETURNING id::text
      """)) {
      statement.setString(1, listingCode);
      statement.setString(2, title);
      statement.setString(3, slug);
      statement.setString(4, clean(request.getString("description", "Inspected clothing item fulfilled and supported by YINILOW.")));
      statement.setString(5, categoryId);
      statement.setBoolean(6, request.getBoolean("pileEligible", true));
      statement.setBigDecimal(7, price);
      statement.setString(8, sellerId);
      try (ResultSet row = statement.executeQuery()) {
        row.next();
        return row.getString("id");
      }
    }
  }

  private static void linkListingUnit(Connection connection, String listingId, String itemUnitId) throws SQLException {
    try (var statement = connection.prepareStatement("""
      INSERT INTO catalog.listing_units (listing_id, item_unit_id, status)
      VALUES (?::uuid, ?::uuid, 'ACTIVE')
      """)) {
      statement.setString(1, listingId);
      statement.setString(2, itemUnitId);
      statement.executeUpdate();
    }
  }

  private static void createImage(Connection connection, String itemUnitId, String imageUrl) throws SQLException {
    try (var statement = connection.prepareStatement("""
      INSERT INTO catalog.item_media (item_unit_id, media_type, url, purpose, approved)
      VALUES (?::uuid, 'IMAGE', ?, 'PRIMARY', true)
      """)) {
      statement.setString(1, itemUnitId);
      statement.setString(2, clean(imageUrl).isBlank() ? "/assets/prod-tee.jpg" : clean(imageUrl));
      statement.executeUpdate();
    }
  }

  private static boolean validImageUrl(String imageUrl) {
    String cleanImageUrl = clean(imageUrl);
    if (cleanImageUrl.isBlank()) {
      return true;
    }
    if (cleanImageUrl.length() > MAX_IMAGE_URL_LENGTH) {
      return false;
    }
    return cleanImageUrl.startsWith("/assets/")
      || cleanImageUrl.startsWith("https://")
      || cleanImageUrl.startsWith("data:image/jpeg;base64,")
      || cleanImageUrl.startsWith("data:image/png;base64,")
      || cleanImageUrl.startsWith("data:image/webp;base64,")
      || cleanImageUrl.startsWith("data:image/svg+xml;base64,");
  }

  private static void createMeasurements(Connection connection, String itemUnitId, JsonObject measurements) throws SQLException {
    for (String key : measurements.fieldNames()) {
      BigDecimal value = amount(measurements.getValue(key));
      if (value.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      try (var statement = connection.prepareStatement("""
        INSERT INTO catalog.measurements (item_unit_id, measurement_type, value, unit)
        VALUES (?::uuid, ?, ?, 'cm')
        """)) {
        statement.setString(1, itemUnitId);
        statement.setString(2, key);
        statement.setBigDecimal(3, value);
        statement.executeUpdate();
      }
    }
  }

  private static String uniqueCode(String prefix, String title) {
    return prefix + "_" + slug(title) + "_" + Long.toString(System.currentTimeMillis(), 36);
  }

  private static String slug(String value) {
    String normalized = Normalizer.normalize(clean(value), Normalizer.Form.NFD)
      .replaceAll("\\p{M}", "")
      .toLowerCase(Locale.ROOT)
      .replaceAll("[^a-z0-9]+", "-")
      .replaceAll("(^-|-$)", "");
    return normalized.isBlank() ? "listing" : normalized;
  }

  private static String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private static BigDecimal amount(Object value) {
    if (value == null || value.toString().isBlank()) {
      return BigDecimal.ZERO;
    }
    return new BigDecimal(value.toString().replace(",", "").trim());
  }
}
