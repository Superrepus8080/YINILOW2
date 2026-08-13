package com.yinilow.catalog;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Optional;

public interface CatalogReader {
  JsonArray categories();

  JsonArray publicListingCards();

  Optional<JsonObject> publicListingDetail(String listingId);

  Optional<Listing> listing(String listingId);
}
