package com.yinilow.catalog;

import com.yinilow.db.Database;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresCatalogService implements CatalogReader {
  private final Database database;

  public PostgresCatalogService(Database database) {
    this.database = database;
  }

  @Override
  public JsonArray categories() {
    try (Connection connection = database.connection();
         var statement = connection.prepareStatement("SELECT name FROM catalog.categories ORDER BY name")) {
      JsonArray categories = new JsonArray();
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          categories.add(rows.getString("name"));
        }
      }
      return categories;
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not load categories", exception);
    }
  }

  @Override
  public JsonArray publicListingCards() {
    JsonArray cards = new JsonArray();
    for (Listing listing : listings()) {
      cards.add(listing.toPublicCard());
    }
    return cards;
  }

  @Override
  public Optional<JsonObject> publicListingDetail(String listingId) {
    return listing(listingId).map(Listing::toPublicDetail);
  }

  @Override
  public Optional<Listing> listing(String listingId) {
    return listings().stream().filter(listing -> listing.listingId().equals(listingId)).findFirst();
  }

  private List<Listing> listings() {
    String sql = """
      SELECT
        pl.public_code,
        iu.unit_code,
        pl.title,
        pl.slug,
        c.name AS category,
        pl.stock_type,
        iu.size_label,
        iu.condition_public,
        pl.public_price,
        pl.currency,
        COALESCE(media.url, '/assets/prod-tee.jpg') AS image_url,
        pl.pile_eligible,
        NOT EXISTS (
          SELECT 1
          FROM inventory.inventory_holds hold
          WHERE hold.item_unit_id = iu.id
            AND hold.status = 'ACTIVE'
            AND hold.expires_at > now()
        ) AS available,
        iu.source_type,
        COALESCE(iu.economic_owner_id::text, '') AS economic_owner_id,
        '' AS custody_holder_id,
        0.00 AS margin,
        COALESCE(measurements.measurements, '{}'::jsonb) AS measurements
      FROM catalog.product_listings pl
      JOIN catalog.categories c ON c.id = pl.category_id
      JOIN catalog.listing_units lu ON lu.listing_id = pl.id AND lu.status = 'ACTIVE'
      JOIN catalog.item_units iu ON iu.id = lu.item_unit_id
      LEFT JOIN LATERAL (
        SELECT url
        FROM catalog.item_media
        WHERE item_unit_id = iu.id AND approved = true
        ORDER BY purpose = 'PRIMARY' DESC, created_at ASC
        LIMIT 1
      ) media ON true
      LEFT JOIN LATERAL (
        SELECT jsonb_object_agg(measurement_type, value || ' ' || unit) AS measurements
        FROM catalog.measurements
        WHERE item_unit_id = iu.id
      ) measurements ON true
      WHERE pl.visibility = 'PUBLIC'
      ORDER BY pl.published_at DESC NULLS LAST, pl.created_at DESC
      """;

    try (Connection connection = database.connection();
         var statement = connection.prepareStatement(sql);
         ResultSet rows = statement.executeQuery()) {
      List<Listing> listings = new ArrayList<>();
      while (rows.next()) {
        listings.add(new Listing(
          rows.getString("public_code"),
          rows.getString("unit_code"),
          rows.getString("title"),
          rows.getString("slug"),
          rows.getString("category"),
          rows.getString("stock_type"),
          rows.getString("size_label"),
          rows.getString("condition_public"),
          rows.getBigDecimal("public_price"),
          rows.getString("currency"),
          rows.getString("image_url"),
          rows.getBoolean("pile_eligible"),
          rows.getBoolean("available"),
          rows.getString("source_type"),
          rows.getString("economic_owner_id"),
          rows.getString("custody_holder_id"),
          rows.getBigDecimal("margin") == null ? BigDecimal.ZERO : rows.getBigDecimal("margin"),
          List.of(),
          new JsonObject(rows.getString("measurements"))
        ));
      }
      return listings;
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not load listings", exception);
    }
  }
}
