package com.yinilow.realtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

class RealtimeTelemetryServiceTest {
  @Test
  void acceptsSafeTelemetryInMemoryMode() {
    RealtimeTelemetryService service = new RealtimeTelemetryService();

    RealtimeTelemetryService.TelemetryResult result = service.record(new JsonObject()
      .put("type", "tag-hover")
      .put("roomId", "dig-pile-main")
      .put("metadata", new JsonObject()
        .put("label", "GHC85")
        .put("viewerCount", 2)
        .put("recentTags", new JsonArray().add("GHC85").add("GHC110"))));

    assertTrue(result.success());
    assertEquals(202, result.statusCode());
    assertEquals("memory", result.payload().getString("stored"));
    assertEquals("GHC85", service.lastMemoryEvent().getJsonObject("metadata").getString("label"));
  }

  @Test
  void rejectsUnsafeTelemetryType() {
    RealtimeTelemetryService service = new RealtimeTelemetryService();

    RealtimeTelemetryService.TelemetryResult result = service.record(new JsonObject()
      .put("type", "checkout")
      .put("roomId", "dig-pile-main"));

    assertEquals(400, result.statusCode());
    assertEquals("REALTIME_TELEMETRY_INVALID", result.payload().getString("error"));
  }

  @Test
  void rejectsUnsafeRoomId() {
    RealtimeTelemetryService service = new RealtimeTelemetryService();

    RealtimeTelemetryService.TelemetryResult result = service.record(new JsonObject()
      .put("type", "heartbeat")
      .put("roomId", "../dig-pile-main"));

    assertEquals(400, result.statusCode());
    assertEquals("REALTIME_TELEMETRY_INVALID", result.payload().getString("error"));
  }
}
