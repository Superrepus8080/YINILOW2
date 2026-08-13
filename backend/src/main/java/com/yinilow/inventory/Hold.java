package com.yinilow.inventory;

import java.time.Instant;

public record Hold(
  String holdId,
  String itemUnitId,
  String listingId,
  String cartId,
  String status,
  Instant expiresAt
) {
}
