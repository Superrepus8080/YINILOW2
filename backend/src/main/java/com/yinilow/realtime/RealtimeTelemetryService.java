package com.yinilow.realtime;

import com.yinilow.db.Database;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public class RealtimeTelemetryService {
  private static final Set<String> SAFE_TYPES = Set.of("presence", "tag-hover", "latency", "heartbeat");
  private volatile Database database;
  private volatile JsonObject lastMemoryEvent = new JsonObject();

  public void useDatabase(Database database) {
    this.database = database;
  }

  public TelemetryResult record(JsonObject request) {
    String type = clean(request.getString("type"));
    String roomId = clean(request.getString("roomId"));
    if (!SAFE_TYPES.contains(type) || roomId.isBlank() || !RealtimeSignalingHub.isAllowedRoomId(roomId)) {
      return TelemetryResult.failure(400, "REALTIME_TELEMETRY_INVALID");
    }

    JsonObject metadata = safeMetadata(request.getJsonObject("metadata", new JsonObject()));
    JsonObject event = new JsonObject()
      .put("type", type)
      .put("roomId", roomId)
      .put("metadata", metadata);

    if (database == null) {
      lastMemoryEvent = event;
      return TelemetryResult.accepted(event.put("stored", "memory"));
    }

    try (Connection connection = database.connection();
         var statement = connection.prepareStatement("""
           INSERT INTO platform.audit_events (action, object_type, metadata)
           VALUES (?, 'REALTIME_ROOM', ?::jsonb)
           """)) {
      statement.setString(1, "REALTIME_" + type.toUpperCase().replace("-", "_"));
      statement.setString(2, event.encode());
      statement.executeUpdate();
      return TelemetryResult.accepted(event.put("stored", "postgres"));
    } catch (SQLException exception) {
      throw new IllegalStateException("Could not record realtime telemetry", exception);
    }
  }

  public JsonObject lastMemoryEvent() {
    return lastMemoryEvent.copy();
  }

  private static JsonObject safeMetadata(JsonObject metadata) {
    JsonObject safe = new JsonObject();
    putString(safe, metadata, "label", 40);
    putString(safe, metadata, "state", 24);
    putNumber(safe, metadata, "viewerCount");
    putNumber(safe, metadata, "latencyMs");
    putNumber(safe, metadata, "at");
    JsonArray recentTags = metadata.getJsonArray("recentTags");
    if (recentTags != null) {
      JsonArray safeTags = new JsonArray();
      for (int index = 0; index < Math.min(recentTags.size(), 5); index++) {
        safeTags.add(clean(String.valueOf(recentTags.getValue(index))).substring(0, Math.min(40, clean(String.valueOf(recentTags.getValue(index))).length())));
      }
      safe.put("recentTags", safeTags);
    }
    return safe;
  }

  private static void putString(JsonObject safe, JsonObject source, String key, int maxLength) {
    String value = clean(source.getString(key));
    if (!value.isBlank()) {
      safe.put(key, value.substring(0, Math.min(maxLength, value.length())));
    }
  }

  private static void putNumber(JsonObject safe, JsonObject source, String key) {
    Number value = source.getNumber(key);
    if (value != null) {
      safe.put(key, value);
    }
  }

  private static String clean(String value) {
    return value == null ? "" : value.trim();
  }

  public record TelemetryResult(boolean success, int statusCode, JsonObject payload) {
    public static TelemetryResult accepted(JsonObject payload) {
      return new TelemetryResult(true, 202, payload);
    }

    public static TelemetryResult failure(int statusCode, String errorCode) {
      return new TelemetryResult(false, statusCode, new JsonObject().put("error", errorCode));
    }
  }
}
