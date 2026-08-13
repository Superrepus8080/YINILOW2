package com.yinilow.catalog;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CatalogService implements CatalogReader {
  private final Map<String, Listing> listings;

  public CatalogService(List<Listing> seedListings) {
    this.listings = new ConcurrentHashMap<>();
    seedListings.forEach(listing -> listings.put(listing.listingId(), listing));
  }

  public static CatalogService seeded() {
    return new CatalogService(List.of(
      new Listing(
        "lst_vintage_polo",
        "unit_vintage_polo",
        "Vintage striped polo",
        "vintage-striped-polo",
        "New Drop",
        "THRIFT_ONE_OFF",
        "M",
        "VERY_GOOD",
        new BigDecimal("85.00"),
        "GHS",
        "/assets/prod-rugby.jpg",
        true,
        true,
        "RESELLER",
        "private_reseller_001",
        "private_custody_001",
        new BigDecimal("18.25"),
        List.of("Light vintage wear disclosed"),
        new JsonObject().put("chest", "52 cm").put("length", "69 cm").put("shoulder", "45 cm")
      ),
      new Listing(
        "lst_leather_jacket",
        "unit_leather_jacket",
        "Vintage Leather Jacket",
        "vintage-leather-jacket",
        "New Drop",
        "THRIFT_ONE_OFF",
        "L",
        "GOOD",
        new BigDecimal("150.00"),
        "GHS",
        "/assets/prod-leather.jpg",
        true,
        true,
        "VENDOR",
        "private_vendor_002",
        "private_custody_002",
        new BigDecimal("31.00"),
        List.of("Minor sleeve creasing"),
        new JsonObject().put("chest", "57 cm").put("length", "72 cm").put("sleeve", "63 cm")
      ),
      new Listing(
        "lst_camo_cargo",
        "unit_camo_cargo",
        "Camo Cargo Pants",
        "camo-cargo-pants",
        "Men",
        "THRIFT_ONE_OFF",
        "32",
        "GOOD",
        new BigDecimal("90.00"),
        "GHS",
        "/assets/prod-cargo.jpg",
        true,
        true,
        "YINILOW",
        "private_yinilow_stock",
        "private_custody_003",
        new BigDecimal("16.00"),
        List.of(),
        new JsonObject().put("waist", "41 cm").put("inseam", "76 cm")
      )
    ));
  }

  public JsonArray categories() {
    return new JsonArray()
      .add("New Drop")
      .add("Women")
      .add("Men")
      .add("Children")
      .add("Shoes")
      .add("Bags & Accessories")
      .add("Dig the Pile")
      .add("Stock Drop");
  }

  public JsonArray publicListingCards() {
    JsonArray response = new JsonArray();
    listings.values().stream()
      .map(Listing::toPublicCard)
      .forEach(response::add);
    return response;
  }

  public Optional<JsonObject> publicListingDetail(String listingId) {
    return listing(listingId).map(Listing::toPublicDetail);
  }

  public Optional<Listing> listing(String listingId) {
    return Optional.ofNullable(listings.get(listingId));
  }
}
