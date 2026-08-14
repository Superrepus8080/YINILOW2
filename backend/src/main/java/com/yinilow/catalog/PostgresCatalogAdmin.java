package com.yinilow.catalog;

import com.yinilow.db.Database;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Locale;

public class PostgresCatalogAdmin implements CatalogAdmin {
  private final Database database;

  public PostgresCatalogAdmin(Database database) {
    this.database = database;
  }

  @Override
  public CreateListingResult createListing(JsonObject request) {
    String title = clean(request.getString("title"));
    String category = clean(request.getString("category", "New Drop"));
    BigDecimal price = amount(request.getValue("price"));
    if (title.isBlank()) {
      return CreateListingResult.failure(400, "TITLE_REQUIRED");
    }
    if (price.compareTo(BigDecimal.ZERO) <= 0) {
      return CreateListingResult.failure(400, "PRICE_REQUIRED");
    }

    try (Connection connection = database.connection()) {
      connection.setAutoCommit(false);
      String categoryId = ensureCategory(connection, category);
      String unitCode = uniqueCode("unit", title);
      String listingCode = uniqueCode("lst", title);
      String itemUnitId = createItemUnit(connection, request, unitCode);
      String listingId = createProductListing(connection, request, listingCode, title, slug(listingCode), categoryId, price);
      linkListingUnit(connection, listingId, itemUnitId);
      createImage(connection, itemUnitId, request.getString("imageUrl", "/assets/prod-tee.jpg"));
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
    BigDecimal price
  ) throws SQLException {
    try (var statement = connection.prepareStatement("""
      INSERT INTO catalog.product_listings (
        public_code, title, slug, description, category_id, stock_type, pile_eligible, restockable,
        return_policy_type, visibility, public_price, currency, published_at
      )
      VALUES (?, ?, ?, ?, ?::uuid, 'THRIFT_ONE_OFF', ?, false, 'THRIFT_LIMITED_RETURN', 'PUBLIC', ?, 'GHS', now())
      RETURNING id::text
      """)) {
      statement.setString(1, listingCode);
      statement.setString(2, title);
      statement.setString(3, slug);
      statement.setString(4, clean(request.getString("description", "Inspected clothing item fulfilled and supported by YINILOW.")));
      statement.setString(5, categoryId);
      statement.setBoolean(6, request.getBoolean("pileEligible", true));
      statement.setBigDecimal(7, price);
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
