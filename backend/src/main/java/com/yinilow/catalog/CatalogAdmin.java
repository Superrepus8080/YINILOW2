package com.yinilow.catalog;

import io.vertx.core.json.JsonObject;

public interface CatalogAdmin {
  CreateListingResult createListing(JsonObject request);

  record CreateListingResult(boolean success, int statusCode, JsonObject payload) {
    public static CreateListingResult created(JsonObject payload) {
      return new CreateListingResult(true, 201, payload);
    }

    public static CreateListingResult failure(int statusCode, String errorCode) {
      return new CreateListingResult(false, statusCode, new JsonObject().put("error", errorCode));
    }
  }
}
