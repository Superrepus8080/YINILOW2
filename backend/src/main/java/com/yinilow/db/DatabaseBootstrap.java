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
      runSql(connection, compatibilitySql());
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

  private static String compatibilitySql() {
    return """
      ALTER TABLE orders.orders
      ALTER COLUMN user_id DROP NOT NULL;

      ALTER TABLE orders.orders
      ADD COLUMN IF NOT EXISTS delivery_address JSONB NOT NULL DEFAULT '{}'::jsonb;

      CREATE SCHEMA IF NOT EXISTS auth;

      CREATE TABLE IF NOT EXISTS auth.seller_accounts (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        email TEXT NOT NULL UNIQUE,
        display_name TEXT NOT NULL,
        password_hash TEXT NOT NULL,
        status TEXT NOT NULL DEFAULT 'ACTIVE',
        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        CONSTRAINT seller_accounts_status_check CHECK (status IN ('ACTIVE', 'SUSPENDED'))
      );

      CREATE TABLE IF NOT EXISTS auth.seller_sessions (
        token_hash TEXT PRIMARY KEY,
        seller_account_id UUID NOT NULL REFERENCES auth.seller_accounts(id),
        expires_at TIMESTAMPTZ NOT NULL,
        revoked_at TIMESTAMPTZ,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
      );

      CREATE INDEX IF NOT EXISTS idx_seller_sessions_account
      ON auth.seller_sessions (seller_account_id);

      CREATE INDEX IF NOT EXISTS idx_seller_sessions_expiry
      ON auth.seller_sessions (expires_at);

      ALTER TABLE catalog.product_listings
      ADD COLUMN IF NOT EXISTS seller_account_id UUID REFERENCES auth.seller_accounts(id);

      INSERT INTO auth.seller_accounts (email, display_name, password_hash)
      VALUES ('seller@yinilow.local', 'YINILOW Seller', '')
      ON CONFLICT (email) DO NOTHING;

      UPDATE catalog.product_listings
      SET seller_account_id = (SELECT id FROM auth.seller_accounts WHERE email = 'seller@yinilow.local')
      WHERE seller_account_id IS NULL;
      """;
  }

  private static String seedSql() {
    return """
      INSERT INTO catalog.categories (name, slug) VALUES
        ('New Drop', 'new-drop'),
        ('Men', 'men'),
        ('Jackets', 'jackets'),
        ('Vests', 'vests'),
        ('Pants', 'pants')
      ON CONFLICT (slug) DO NOTHING;

      INSERT INTO cart.carts (id, status)
      VALUES ('%s', 'ACTIVE')
      ON CONFLICT (id) DO NOTHING;

      INSERT INTO catalog.item_units (
        id, stock_type, unit_code, source_type, economic_owner_type, status, size_label, condition_public
      ) VALUES
        ('10000000-0000-0000-0000-000000000001', 'THRIFT_ONE_OFF', 'unit_vintage_polo', 'RESELLER', 'RESELLER', 'LISTED', 'M', 'VERY_GOOD'),
        ('10000000-0000-0000-0000-000000000002', 'THRIFT_ONE_OFF', 'unit_leather_jacket', 'VENDOR', 'VENDOR', 'LISTED', 'L', 'GOOD'),
        ('10000000-0000-0000-0000-000000000003', 'THRIFT_ONE_OFF', 'unit_camo_cargo', 'YINILOW', 'YINILOW', 'LISTED', '32', 'GOOD'),
        ('10000000-0000-0000-0000-000000000101', 'THRIFT_ONE_OFF', 'unit_black_bomber_jacket', 'YINILOW', 'YINILOW', 'LISTED', '3XL', 'GOOD'),
        ('10000000-0000-0000-0000-000000000102', 'THRIFT_ONE_OFF', 'unit_tan_work_vest', 'YINILOW', 'YINILOW', 'LISTED', '2XL', 'GOOD'),
        ('10000000-0000-0000-0000-000000000103', 'THRIFT_ONE_OFF', 'unit_black_red_bomber', 'YINILOW', 'YINILOW', 'LISTED', 'XL', 'VERY_GOOD'),
        ('10000000-0000-0000-0000-000000000104', 'THRIFT_ONE_OFF', 'unit_olive_utility_vest', 'YINILOW', 'YINILOW', 'LISTED', 'XL', 'GOOD'),
        ('10000000-0000-0000-0000-000000000105', 'THRIFT_ONE_OFF', 'unit_navy_hooded_jacket', 'YINILOW', 'YINILOW', 'LISTED', 'XL', 'GOOD'),
        ('10000000-0000-0000-0000-000000000106', 'THRIFT_ONE_OFF', 'unit_adidas_track_jacket', 'YINILOW', 'YINILOW', 'LISTED', 'XL', 'GOOD'),
        ('10000000-0000-0000-0000-000000000107', 'THRIFT_ONE_OFF', 'unit_red_ohio_pullover', 'YINILOW', 'YINILOW', 'LISTED', 'L', 'GOOD'),
        ('10000000-0000-0000-0000-000000000108', 'THRIFT_ONE_OFF', 'unit_blue_varsity_windbreaker', 'YINILOW', 'YINILOW', 'LISTED', 'L', 'VERY_GOOD'),
        ('10000000-0000-0000-0000-000000000109', 'THRIFT_ONE_OFF', 'unit_green_miami_windbreaker', 'YINILOW', 'YINILOW', 'LISTED', 'L', 'GOOD'),
        ('10000000-0000-0000-0000-000000000110', 'THRIFT_ONE_OFF', 'unit_grey_work_jacket', 'YINILOW', 'YINILOW', 'LISTED', 'L', 'GOOD'),
        ('10000000-0000-0000-0000-000000000111', 'THRIFT_ONE_OFF', 'unit_red_rain_shell', 'YINILOW', 'YINILOW', 'LISTED', 'L', 'GOOD'),
        ('10000000-0000-0000-0000-000000000112', 'THRIFT_ONE_OFF', 'unit_black_red_adidas_shell', 'YINILOW', 'YINILOW', 'LISTED', 'L', 'GOOD'),
        ('10000000-0000-0000-0000-000000000113', 'THRIFT_ONE_OFF', 'unit_stone_work_pants', 'YINILOW', 'YINILOW', 'LISTED', 'W46', 'GOOD'),
        ('10000000-0000-0000-0000-000000000114', 'THRIFT_ONE_OFF', 'unit_blue_levis_denim', 'YINILOW', 'YINILOW', 'LISTED', 'W44', 'GOOD'),
        ('10000000-0000-0000-0000-000000000115', 'THRIFT_ONE_OFF', 'unit_grey_work_pants', 'YINILOW', 'YINILOW', 'LISTED', 'W44', 'GOOD'),
        ('10000000-0000-0000-0000-000000000116', 'THRIFT_ONE_OFF', 'unit_khaki_cargo_pants', 'YINILOW', 'YINILOW', 'LISTED', 'W44', 'GOOD'),
        ('10000000-0000-0000-0000-000000000117', 'THRIFT_ONE_OFF', 'unit_light_blue_denim', 'YINILOW', 'YINILOW', 'LISTED', 'W42', 'GOOD'),
        ('10000000-0000-0000-0000-000000000118', 'THRIFT_ONE_OFF', 'unit_black_work_pants', 'YINILOW', 'YINILOW', 'LISTED', 'W40', 'VERY_GOOD'),
        ('10000000-0000-0000-0000-000000000119', 'THRIFT_ONE_OFF', 'unit_olive_cargo_pants', 'YINILOW', 'YINILOW', 'LISTED', 'W40', 'GOOD')
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
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 90.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000101', 'lst_black_bomber_jacket', 'Vintage Black Bomber Jacket', 'vintage-black-bomber-jacket',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 125.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000102', 'lst_tan_work_vest', 'Tan Work Vest', 'tan-work-vest',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'vests'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 95.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000103', 'lst_black_red_bomber', 'Black Red Bomber Jacket', 'black-red-bomber-jacket',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 110.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000104', 'lst_olive_utility_vest', 'Olive Utility Vest', 'olive-utility-vest',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'vests'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 85.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000105', 'lst_navy_hooded_jacket', 'Navy Hooded Jacket', 'navy-hooded-jacket',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 140.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000106', 'lst_adidas_track_jacket', 'Adidas Track Jacket', 'adidas-track-jacket',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 120.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000107', 'lst_red_ohio_pullover', 'Red Ohio Pullover', 'red-ohio-pullover',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 90.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000108', 'lst_blue_varsity_windbreaker', 'Blue Varsity Windbreaker', 'blue-varsity-windbreaker',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 115.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000109', 'lst_green_miami_windbreaker', 'Green Miami Windbreaker', 'green-miami-windbreaker',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 105.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000110', 'lst_grey_work_jacket', 'Grey Work Jacket', 'grey-work-jacket',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 130.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000111', 'lst_red_rain_shell', 'Red Rain Shell', 'red-rain-shell',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 80.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000112', 'lst_black_red_adidas_shell', 'Black Red Adidas Shell', 'black-red-adidas-shell',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'jackets'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 125.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000113', 'lst_stone_work_pants', 'Stone Work Pants W46', 'stone-work-pants-w46',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'pants'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 110.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000114', 'lst_blue_levis_denim', 'Blue Levi''s Denim W44', 'blue-levis-denim-w44',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'pants'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 125.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000115', 'lst_grey_work_pants', 'Grey Work Pants W44', 'grey-work-pants-w44',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'pants'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 105.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000116', 'lst_khaki_cargo_pants', 'Khaki Cargo Pants W44', 'khaki-cargo-pants-w44',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'pants'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 115.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000117', 'lst_light_blue_denim', 'Light Blue Denim W42', 'light-blue-denim-w42',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'pants'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 100.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000118', 'lst_black_work_pants', 'Black Work Pants W40', 'black-work-pants-w40',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'pants'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 110.00, 'GHS', now()),
        ('20000000-0000-0000-0000-000000000119', 'lst_olive_cargo_pants', 'Olive Cargo Pants W40', 'olive-cargo-pants-w40',
          'Freshly cropped from the YINILOW pile. Inspected, photographed, and ready for local checkout.',
          (SELECT id FROM catalog.categories WHERE slug = 'pants'), 'THRIFT_ONE_OFF', true, false,
          'THRIFT_LIMITED_RETURN', 'PUBLIC', 105.00, 'GHS', now())
      ON CONFLICT (public_code) DO NOTHING;

      INSERT INTO catalog.listing_units (listing_id, item_unit_id, status) VALUES
        ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000101', '10000000-0000-0000-0000-000000000101', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000102', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000103', '10000000-0000-0000-0000-000000000103', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000104', '10000000-0000-0000-0000-000000000104', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000105', '10000000-0000-0000-0000-000000000105', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000106', '10000000-0000-0000-0000-000000000106', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000107', '10000000-0000-0000-0000-000000000107', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000108', '10000000-0000-0000-0000-000000000108', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000109', '10000000-0000-0000-0000-000000000109', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000110', '10000000-0000-0000-0000-000000000110', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000111', '10000000-0000-0000-0000-000000000111', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000112', '10000000-0000-0000-0000-000000000112', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000113', '10000000-0000-0000-0000-000000000113', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000114', '10000000-0000-0000-0000-000000000114', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000115', '10000000-0000-0000-0000-000000000115', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000116', '10000000-0000-0000-0000-000000000116', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000117', '10000000-0000-0000-0000-000000000117', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000118', '10000000-0000-0000-0000-000000000118', 'ACTIVE'),
        ('20000000-0000-0000-0000-000000000119', '10000000-0000-0000-0000-000000000119', 'ACTIVE')
      ON CONFLICT (listing_id, item_unit_id) DO NOTHING;

      INSERT INTO catalog.item_media (item_unit_id, media_type, url, purpose, approved) VALUES
        ('10000000-0000-0000-0000-000000000001'::uuid, 'IMAGE', '/assets/prod-rugby.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000002'::uuid, 'IMAGE', '/assets/prod-leather.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000003'::uuid, 'IMAGE', '/assets/prod-cargo.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000101'::uuid, 'IMAGE', '/assets/catalog/drop-black-bomber.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000102'::uuid, 'IMAGE', '/assets/catalog/drop-tan-work-vest.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000103'::uuid, 'IMAGE', '/assets/catalog/drop-black-red-bomber.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000104'::uuid, 'IMAGE', '/assets/catalog/drop-olive-utility-vest.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000105'::uuid, 'IMAGE', '/assets/catalog/drop-navy-hooded-jacket.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000106'::uuid, 'IMAGE', '/assets/catalog/drop-adidas-track-jacket.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000107'::uuid, 'IMAGE', '/assets/catalog/drop-red-ohio-pullover.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000108'::uuid, 'IMAGE', '/assets/catalog/drop-blue-varsity-jacket.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000109'::uuid, 'IMAGE', '/assets/catalog/drop-green-miami-windbreaker.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000110'::uuid, 'IMAGE', '/assets/catalog/drop-grey-work-jacket.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000111'::uuid, 'IMAGE', '/assets/catalog/drop-red-rain-shell.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000112'::uuid, 'IMAGE', '/assets/catalog/drop-black-red-adidas-shell.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000113'::uuid, 'IMAGE', '/assets/catalog/denim-stone-work-pants.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000114'::uuid, 'IMAGE', '/assets/catalog/denim-blue-levis-denim.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000115'::uuid, 'IMAGE', '/assets/catalog/denim-grey-work-pants.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000116'::uuid, 'IMAGE', '/assets/catalog/denim-khaki-cargo-pants.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000117'::uuid, 'IMAGE', '/assets/catalog/denim-light-blue-denim.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000118'::uuid, 'IMAGE', '/assets/catalog/denim-black-work-pants.jpg', 'PRIMARY', true),
        ('10000000-0000-0000-0000-000000000119'::uuid, 'IMAGE', '/assets/catalog/denim-olive-cargo-pants.jpg', 'PRIMARY', true)
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

      UPDATE catalog.product_listings
      SET visibility = 'HIDDEN', updated_at = now()
      WHERE public_code LIKE 'lst_uploaded-photo-test-%%'
         OR public_code LIKE 'lst_owner-scoped-jacket-%%'
         OR public_code LIKE 'lst_fresh-y2k-denim-jacket_%%'
         OR public_code LIKE 'lst_live-test-denim-jacket_%%';
      """.formatted(CartService.DEMO_CART_ID);
  }
}
