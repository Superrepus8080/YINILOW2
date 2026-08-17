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
      ),
      // Freshly cropped pile stock. Keeping the in-memory catalog aligned with the
      // PostgreSQL seed means the storefront shows a full grid even without a
      // database, instead of collapsing to three items.
      pileItem("lst_black_bomber_jacket", "unit_black_bomber_jacket", "Vintage Black Bomber Jacket", "vintage-black-bomber-jacket", "Jackets", "3XL", "GOOD", "125.00", "/assets/catalog/drop-black-bomber.jpg"),
      pileItem("lst_tan_work_vest", "unit_tan_work_vest", "Tan Work Vest", "tan-work-vest", "Vests", "2XL", "GOOD", "95.00", "/assets/catalog/drop-tan-work-vest.jpg"),
      pileItem("lst_black_red_bomber", "unit_black_red_bomber", "Black Red Bomber Jacket", "black-red-bomber-jacket", "Jackets", "XL", "VERY_GOOD", "110.00", "/assets/catalog/drop-black-red-bomber.jpg"),
      pileItem("lst_olive_utility_vest", "unit_olive_utility_vest", "Olive Utility Vest", "olive-utility-vest", "Vests", "XL", "GOOD", "85.00", "/assets/catalog/drop-olive-utility-vest.jpg"),
      pileItem("lst_navy_hooded_jacket", "unit_navy_hooded_jacket", "Navy Hooded Jacket", "navy-hooded-jacket", "Jackets", "XL", "GOOD", "140.00", "/assets/catalog/drop-navy-hooded-jacket.jpg"),
      pileItem("lst_adidas_track_jacket", "unit_adidas_track_jacket", "Adidas Track Jacket", "adidas-track-jacket", "Jackets", "XL", "GOOD", "120.00", "/assets/catalog/drop-adidas-track-jacket.jpg"),
      pileItem("lst_red_ohio_pullover", "unit_red_ohio_pullover", "Red Ohio Pullover", "red-ohio-pullover", "Jackets", "L", "GOOD", "90.00", "/assets/catalog/drop-red-ohio-pullover.jpg"),
      pileItem("lst_blue_varsity_windbreaker", "unit_blue_varsity_windbreaker", "Blue Varsity Windbreaker", "blue-varsity-windbreaker", "Jackets", "L", "VERY_GOOD", "115.00", "/assets/catalog/drop-blue-varsity-jacket.jpg"),
      pileItem("lst_green_miami_windbreaker", "unit_green_miami_windbreaker", "Green Miami Windbreaker", "green-miami-windbreaker", "Jackets", "L", "GOOD", "105.00", "/assets/catalog/drop-green-miami-windbreaker.jpg"),
      pileItem("lst_grey_work_jacket", "unit_grey_work_jacket", "Grey Work Jacket", "grey-work-jacket", "Jackets", "L", "GOOD", "130.00", "/assets/catalog/drop-grey-work-jacket.jpg"),
      pileItem("lst_red_rain_shell", "unit_red_rain_shell", "Red Rain Shell", "red-rain-shell", "Jackets", "L", "GOOD", "80.00", "/assets/catalog/drop-red-rain-shell.jpg"),
      pileItem("lst_black_red_adidas_shell", "unit_black_red_adidas_shell", "Black Red Adidas Shell", "black-red-adidas-shell", "Jackets", "L", "GOOD", "125.00", "/assets/catalog/drop-black-red-adidas-shell.jpg"),
      pileItem("lst_stone_work_pants", "unit_stone_work_pants", "Stone Work Pants W46", "stone-work-pants-w46", "Pants", "W46", "GOOD", "110.00", "/assets/catalog/denim-stone-work-pants.jpg"),
      pileItem("lst_blue_levis_denim", "unit_blue_levis_denim", "Blue Levi's Denim W44", "blue-levis-denim-w44", "Pants", "W44", "GOOD", "125.00", "/assets/catalog/denim-blue-levis-denim.jpg"),
      pileItem("lst_grey_work_pants", "unit_grey_work_pants", "Grey Work Pants W44", "grey-work-pants-w44", "Pants", "W44", "GOOD", "105.00", "/assets/catalog/denim-grey-work-pants.jpg"),
      pileItem("lst_khaki_cargo_pants", "unit_khaki_cargo_pants", "Khaki Cargo Pants W44", "khaki-cargo-pants-w44", "Pants", "W44", "GOOD", "115.00", "/assets/catalog/denim-khaki-cargo-pants.jpg"),
      pileItem("lst_light_blue_denim", "unit_light_blue_denim", "Light Blue Denim W42", "light-blue-denim-w42", "Pants", "W42", "GOOD", "100.00", "/assets/catalog/denim-light-blue-denim.jpg"),
      pileItem("lst_black_work_pants", "unit_black_work_pants", "Black Work Pants W40", "black-work-pants-w40", "Pants", "W40", "VERY_GOOD", "110.00", "/assets/catalog/denim-black-work-pants.jpg"),
      pileItem("lst_olive_cargo_pants", "unit_olive_cargo_pants", "Olive Cargo Pants W40", "olive-cargo-pants-w40", "Pants", "W40", "GOOD", "105.00", "/assets/catalog/denim-olive-cargo-pants.jpg")
    ));
  }

  private static Listing pileItem(
    String listingId,
    String itemUnitId,
    String title,
    String slug,
    String category,
    String sizeLabel,
    String conditionPublic,
    String price,
    String imageUrl
  ) {
    return new Listing(
      listingId,
      itemUnitId,
      title,
      slug,
      category,
      "THRIFT_ONE_OFF",
      sizeLabel,
      conditionPublic,
      new BigDecimal(price),
      "GHS",
      imageUrl,
      true,
      true,
      "YINILOW",
      "private_yinilow_stock",
      "private_custody",
      new BigDecimal("20.00"),
      List.of(),
      new JsonObject()
    );
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
