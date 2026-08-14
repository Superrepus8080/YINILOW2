package com.yinilow.inventory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HoldService implements HoldManager {
  private static final Duration DEFAULT_HOLD_TIME = Duration.ofMinutes(10);
  private final Clock clock;
  private final Map<String, Hold> activeHoldByItemUnit = new ConcurrentHashMap<>();

  public HoldService() {
    this(Clock.systemUTC());
  }

  public HoldService(Clock clock) {
    this.clock = clock;
  }

  public HoldResult createHold(String itemUnitId, String listingId, String cartId) {
    expireIfNeeded(itemUnitId);
    Instant expiresAt = clock.instant().plus(DEFAULT_HOLD_TIME);
    Hold hold = new Hold("hold_" + UUID.randomUUID(), itemUnitId, listingId, cartId, "ACTIVE", expiresAt);
    Hold existing = activeHoldByItemUnit.putIfAbsent(itemUnitId, hold);
    if (existing != null && existing.expiresAt().isAfter(clock.instant())) {
      return HoldResult.conflict(existing);
    }
    if (existing != null) {
      activeHoldByItemUnit.replace(itemUnitId, existing, hold);
    }
    return HoldResult.created(hold);
  }

  public Optional<Hold> activeHoldFor(String itemUnitId) {
    expireIfNeeded(itemUnitId);
    return Optional.ofNullable(activeHoldByItemUnit.get(itemUnitId));
  }

  public boolean isActive(String itemUnitId) {
    return activeHoldFor(itemUnitId).isPresent();
  }

  private void expireIfNeeded(String itemUnitId) {
    Hold existing = activeHoldByItemUnit.get(itemUnitId);
    if (existing != null && !existing.expiresAt().isAfter(clock.instant())) {
      activeHoldByItemUnit.remove(itemUnitId, existing);
    }
  }

  public record HoldResult(boolean created, Hold hold, String errorCode) {
    static HoldResult created(Hold hold) {
      return new HoldResult(true, hold, null);
    }

    static HoldResult conflict(Hold hold) {
      return new HoldResult(false, hold, "ITEM_HELD");
    }

    static HoldResult unavailable() {
      return new HoldResult(false, null, "ITEM_UNAVAILABLE");
    }
  }
}
