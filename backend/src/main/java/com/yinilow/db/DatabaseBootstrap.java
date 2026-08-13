package com.yinilow.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.yinilow.cart.CartService;

public class DatabaseBootstrap {
  private final Database database;

  public DatabaseBootstrap(Database database) {
    this.database = database;
  }

  public void migrateAndSeed() {
    try (Connection connection = database.connection()) {
      if (!schemaExists(connection)) {
        runSql(connection, migrationSql());
      }
      runSql(connection, seedSql());
    } catch (SQLException | IOException exception) {
      throw new IllegalStateException("Database bootstrap failed", exception);
    }
  }

  private static boolean schemaExists(Connection connection) throws SQLException {
    try (var statement = connection.prepareStatement("SELECT to_regclass('catalog.product_listings') IS NOT NULL");
         var row = statement.executeQuery()) {
      row.next();
      return row.getBoolean(1);
    }
  }

  private static void runSql(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  private static String migrationSql() throws IOException {
    try (InputStream stream = DatabaseBootstrap.class.getResourceAsStream("/db/migration/V001__core_clothing_os.sql")) {
      if (stream == null) {
        throw new IOException("Missing database migration");
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static String seedSql() {
    return """
      INSERT INTO catalog.categories (name, slug) VALUES
        ('New Drop', 'new-drop'),
        ('Men', 'men')
      ON CONFLICT (slug) DO NOTHING;

      INSERT INTO cart.carts (id, status)
      VALUES ('%s', 'ACTIVE')
      ON CONFLICT (id) DO NOTHING;

      INSERT INTO catalog.item_units (
        id, stock_type, unit_code, source_type, economic_owner_type, status, size_label, condition_public
      ) VALUES
        ('10000000-0000-0000-0000-000000000001', 'THRIFT_ONE_OFF', 'unit_vintage_polo', 'RESELLER', 'RESELLER', 'LISTED', 'M', 'VERY_GOOD'),
        ('10000000-0000-0000-0000-000000000002', 'THRIFT_ONE_OFF', 'unit_leather_jacket', 'VENDOR', 'VENDOR', 'LISTED', 'L', 'GOOD'),
        ('10000000-0000-0000-0000-000000000003', 'THRIFT_ONE_OFF', 'unit_camo_cargo', 'YINILOW', 'YINILOW', 'LISTED', '32', 'GOOD')
      ON CONFLICT (unit_code) DO NOTHING;

      INSERT INTO catalog.product_listings (
        id, public_code, title, slug, description, category_id, stock_type, pile_eligible, restockable,
        return_policy_type, visibility, public_price, currency, published_at
      ) VALUES
        ('20000000-0000-0000-0000-000000000001', 'lst_vintage_polo', 'Vintage striped polo', 'vintage-striped-polo',
          'Inspected clothing item fulfilled and supported by YINILOW.',
          (SELECT id FROM catalog.categories WHERE slug = 'new-drop'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 85.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000002', 'lst_leather_jacket', 'Vintage Leather Jacket', 'vintage-leather-jacket',
          'Inspected clothing item fulfilled and supported by YINILOW.',
          (SELECT id FROM catalog.categories WHERE slug = 'new-drop'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 150.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000003', 'lst_camo_cargo', 'Camo Cargo Pants', 'camo-cargo-pants',
          'Inspected clothing item fulfilled and supported by YINILOW.',
          (SELECT id FROM catalog.categories WHERE slug = 'men'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 90.00, 'GHS', now())
      ON CONFLICT (public_code) DO NOTHING;

      INSERT INTO catalog.listing_units (listing_id, item_unit_id, status) VALUES
        ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'ACTIVE')
      ON CONFLICT (listing_id, item_unit_id) DO NOTHING;

      INSERT INTO catalog.item_media (item_unit_id, media_type, url, purpose, approved) VALUES
        ('10000000-0000-0000-0000-000000000001'::uuid, 'IMAGE', '/assets/prod-rugby.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000002'::uuid, 'IMAGE', '/assets/prod-leather.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000003'::uuid, 'IMAGE', '/assets/prod-cargo.jpg', 'PRIMARY', true)
      EXCEPT
      SELECT item_unit_id, media_type, url, purpose, approved
      FROM catalog.item_media;

      INSERT INTO catalog.measurements (item_unit_id, measurement_type, value, unit) VALUES
        ('10000000-0000-0000-0000-000000000001'::uuid, 'chest', 52, 'cm'),
        ('10000000-0000-0000-0000-000000000001'::uuid, 'length', 69, 'cm'),
        ('10000000-0000-0000-0000-000000000001'::uuid, 'shoulder', 45, 'cm'),
        ('10000000-0000-0000-0000-000000000002'::uuid, 'chest', 57, 'cm'),
        ('10000000-0000-0000-0000-000000000002'::uuid, 'length', 72, 'cm'),
        ('10000000-0000-0000-0000-000000000002'::uuid, 'sleeve', 63, 'cm'),
        ('10000000-0000-0000-0000-000000000003'::uuid, 'waist', 41, 'cm'),
        ('10000000-0000-0000-0000-000000000003'::uuid, 'inseam', 76, 'cm')
      EXCEPT
      SELECT item_unit_id, measurement_type, value, unit
      FROM catalog.measurements;
      """.formatted(CartService.DEMO_CART_ID);
  }
}
