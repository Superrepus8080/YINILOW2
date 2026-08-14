package com.yinilow.inventory;

import com.yinilow.db.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class PostgresHoldService implements HoldManager {
  private final Database database;

  public PostgresHoldService(Database database) {
    this.database = database;
  }

  @Override
  public HoldService.HoldResult createHold(String itemUnitId, String listingId, String cartId) {
    try (Connection connection = database.connection()) {
      connection.setAutoCommit(false);
      expireOldHolds(connection);
      Optional<Hold> existing = activeHoldFor(connection, itemUnitId);
      if (existing.isPresent()) {
        connection.rollback();
        return HoldService.HoldResult.conflict(existing.get());
      }

      if (!isHoldable(connection, itemUnitId, listingId)) {
        connection.rollback();
        return HoldService.HoldResult.unavailable();
      }

      Hold hold = insertHold(connection, itemUnitId, listingId, cartId);
      connection.commit();
      return HoldService.HoldResult.created(hold);
    } catch (SQLException exception) {
      if ("23505".equals(exception.getSQLState())) {
        return activeHoldFor(itemUnitId)
          .map(HoldService.HoldResult::conflict)
          .orElseThrow(() -> new IllegalStateException("Database hold conflict without active hold", exception));
      }
      throw new IllegalStateException("Could not create inventory hold", exception);
    }
  }

  @Override
  public Optional<Hold> activeHoldFor(String itemUnitId) {
    try (Connection connection = database.connection()) {
      expireOldHolds(connection);
      return activeHoldFor(connection, itemUnitId);
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not load inventory hold", exception);
    }
  }

  @Override
  public boolean isActive(String itemUnitId) {
    return activeHoldFor(itemUnitId).isPresent();
  }

  private static void expireOldHolds(Connection connection) throws SQLException {
    try (var statement = connection.prepareStatement("""
      UPDATE inventory.inventory_holds
      SET status = 'EXPIRED'
      WHERE status = 'ACTIVE' AND expires_at <= now()
      """)) {
      statement.executeUpdate();
    }
  }

  private static Optional<Hold> activeHoldFor(Connection connection, String itemUnitCode) throws SQLException {
    try (var statement = connection.prepareStatement("""
      SELECT hold.id::text AS hold_id, unit.unit_code, listing.public_code, hold.cart_id::text AS cart_id, hold.status, hold.expires_at
      FROM inventory.inventory_holds hold
      JOIN catalog.item_units unit ON unit.id = hold.item_unit_id
      JOIN catalog.product_listings listing ON listing.id = hold.listing_id
      WHERE unit.unit_code = ?
        AND hold.status = 'ACTIVE'
        AND hold.expires_at > now()
      LIMIT 1
      """)) {
      statement.setString(1, itemUnitCode);
      try (ResultSet row = statement.executeQuery()) {
        if (!row.next()) {
          return Optional.empty();
        }
        return Optional.of(toHold(row));
      }
    }
  }

  private static boolean isHoldable(Connection connection, String itemUnitCode, String listingPublicCode) throws SQLException {
    try (var statement = connection.prepareStatement("""
      SELECT 1
      FROM catalog.product_listings listing
      JOIN catalog.listing_units listing_unit ON listing_unit.listing_id = listing.id
      JOIN catalog.item_units unit ON unit.id = listing_unit.item_unit_id
      WHERE listing.public_code = ?
        AND unit.unit_code = ?
        AND listing.visibility = 'PUBLIC'
        AND listing_unit.status = 'ACTIVE'
        AND unit.status = 'LISTED'
      LIMIT 1
      """)) {
      statement.setString(1, listingPublicCode);
      statement.setString(2, itemUnitCode);
      try (ResultSet row = statement.executeQuery()) {
        return row.next();
      }
    }
  }

  private static Hold insertHold(Connection connection, String itemUnitCode, String listingPublicCode, String cartId) throws SQLException {
    try (var statement = connection.prepareStatement("""
      INSERT INTO inventory.inventory_holds (item_unit_id, listing_id, cart_id, hold_source, status, expires_at)
      VALUES (
        (SELECT id FROM catalog.item_units WHERE unit_code = ?),
        (SELECT id FROM catalog.product_listings WHERE public_code = ?),
        ?::uuid,
        'ADD_TO_BAG',
        'ACTIVE',
        now() + interval '10 minutes'
      )
      RETURNING id::text AS hold_id,
        (SELECT unit_code FROM catalog.item_units WHERE unit_code = ?) AS unit_code,
        (SELECT public_code FROM catalog.product_listings WHERE public_code = ?) AS public_code,
        cart_id::text,
        status,
        expires_at
      """)) {
      statement.setString(1, itemUnitCode);
      statement.setString(2, listingPublicCode);
      statement.setString(3, cartId);
      statement.setString(4, itemUnitCode);
      statement.setString(5, listingPublicCode);
      try (ResultSet row = statement.executeQuery()) {
        row.next();
        return toHold(row);
      }
    }
  }

  private static Hold toHold(ResultSet row) throws SQLException {
    Timestamp expiresAt = row.getTimestamp("expires_at");
    return new Hold(
      "hold_" + row.getString("hold_id"),
      row.getString("unit_code"),
      row.getString("public_code"),
      row.getString("cart_id"),
      row.getString("status"),
      expiresAt.toInstant()
    );
  }
}
