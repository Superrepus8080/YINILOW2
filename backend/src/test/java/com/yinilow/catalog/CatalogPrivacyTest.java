package com.yinilow.catalog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

class CatalogPrivacyTest {
  @Test
  void publicListingDetailHidesPrivateOwnershipFields() {
    CatalogService catalog = CatalogService.seeded();
    JsonObject detail = catalog.publicListingDetail("lst_vintage_polo").orElseThrow();

    assertTrue(detail.getBoolean("forbiddenFieldsOmitted"));
    assertFalse(detail.containsKey("sourceType"));
    assertFalse(detail.containsKey("economicOwnerId"));
    assertFalse(detail.containsKey("custodyHolderId"));
    assertFalse(detail.containsKey("margin"));
    assertFalse(detail.containsKey("vendorId"));
    assertFalse(detail.containsKey("resellerId"));
  }
}
