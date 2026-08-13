package com.yinilow.catalog;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import java.util.List;

public record Listing(
  String listingId,
  String itemUnitId,
  String title,
  String slug,
  String category,
  String stockType,
  String sizeLabel,
  String conditionPublic,
  BigDecimal price,
  String currency,
  String imageUrl,
  boolean pileEligible,
  boolean available,
  String sourceType,
  String economicOwnerId,
  String custodyHolderId,
  BigDecimal margin,
  List<String> publicDefects,
  JsonObject measurements
) {
  public JsonObject toPublicCard() {
    return new JsonObject()
      .put("listingId", listingId)
      .put("title", title)
      .put("slug", slug)
      .put("category", category)
      .put("stockType", stockType)
      .put("stockLabel", stockType.equals("THRIFT_ONE_OFF") ? "Only 1 left" : "Limited stock")
      .put("sizeLabel", sizeLabel)
      .put("conditionPublic", conditionPublic)
      .put("price", price)
      .put("currency", currency)
      .put("imageUrl", imageUrl)
      .put("pileEligible", pileEligible)
      .put("availabilityState", available ? "AVAILABLE" : "UNAVAILABLE")
      .put("sellerTrustLabel", "YINILOW Verified");
  }

  public JsonObject toPublicDetail() {
    return toPublicCard()
      .put("description", "Inspected clothing item fulfilled and supported by YINILOW.")
      .put("gallery", new JsonArray().add(imageUrl))
      .put("publicDefects", new JsonArray(publicDefects))
      .put("measurements", measurements)
      .put("returnPolicyType", stockType.equals("THRIFT_ONE_OFF") ? "THRIFT_LIMITED_RETURN" : "STANDARD_CLOTHING_RETURN")
      .put("deliveryEstimate", "Fast local delivery in Ghana")
      .put("forbiddenFieldsOmitted", true);
  }
}
