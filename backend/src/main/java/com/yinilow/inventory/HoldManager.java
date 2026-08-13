package com.yinilow.inventory;

import java.util.Optional;

public interface HoldManager {
  HoldService.HoldResult createHold(String itemUnitId, String listingId, String cartId);

  Optional<Hold> activeHoldFor(String itemUnitId);

  boolean isActive(String itemUnitId);
}
