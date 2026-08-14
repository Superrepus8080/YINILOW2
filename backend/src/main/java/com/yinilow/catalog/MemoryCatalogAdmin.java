package com.yinilow.catalog;

import io.vertx.core.json.JsonObject;

public class MemoryCatalogAdmin implements CatalogAdmin {
  @Override
  public CreateListingResult createListing(JsonObject request) {
    if (request.getString("title", "").isBlank()) {
      return CreateListingResult.failure(400, "TITLE_REQUIRED");
    }
    return CreateListingResult.created(new JsonObject()
      .put("listingId", "lst_preview_" + System.currentTimeMillis())
      .put("title", request.getString("title"))
      .put("status", "PREVIEW_ONLY"));
  }
}
