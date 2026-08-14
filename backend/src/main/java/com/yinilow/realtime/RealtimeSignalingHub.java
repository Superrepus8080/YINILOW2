package com.yinilow.realtime;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class RealtimeSignalingHub {
  private static final int MAX_SIGNAL_BYTES = 16_384;
  private static final Pattern SAFE_ROOM = Pattern.compile("[a-zA-Z0-9:_-]{3,96}");
  private final ConcurrentHashMap<String, Set<ServerWebSocket>> rooms = new ConcurrentHashMap<>();

  public void handle(RoutingContext context) {
    String roomId = context.pathParam("roomId");
    if (!isAllowedRoomId(roomId)) {
      context.response().setStatusCode(400).end("INVALID_ROOM");
      return;
    }

    context.request().toWebSocket()
      .onSuccess(socket -> join(roomId, socket))
      .onFailure(context::fail);
  }

  static boolean isAllowedRoomId(String roomId) {
    return roomId != null && SAFE_ROOM.matcher(roomId).matches();
  }

  private void join(String roomId, ServerWebSocket socket) {
    String peerId = UUID.randomUUID().toString();
    rooms.computeIfAbsent(roomId, ignored -> ConcurrentHashMap.newKeySet()).add(socket);
    socket.writeTextMessage(new JsonObject()
      .put("type", "joined")
      .put("peerId", peerId)
      .put("roomId", roomId)
      .encode());

    socket.textMessageHandler(message -> relay(roomId, peerId, socket, message));
    socket.closeHandler(ignored -> leave(roomId, socket));
    socket.exceptionHandler(ignored -> leave(roomId, socket));
  }

  private void relay(String roomId, String peerId, ServerWebSocket sender, String message) {
    if (message == null || message.length() > MAX_SIGNAL_BYTES) {
      sender.close((short) 1009, "SIGNAL_TOO_LARGE");
      return;
    }

    JsonObject payload;
    try {
      payload = new JsonObject(message);
    } catch (DecodeException exception) {
      sender.writeTextMessage(new JsonObject().put("type", "error").put("code", "INVALID_SIGNAL").encode());
      return;
    }

    String type = payload.getString("type", "");
    if (!Set.of("ready", "offer", "answer", "ice", "leave").contains(type)) {
      sender.writeTextMessage(new JsonObject().put("type", "error").put("code", "UNSUPPORTED_SIGNAL").encode());
      return;
    }

    JsonObject outbound = payload
      .put("fromPeerId", peerId)
      .put("roomId", roomId);

    Set<ServerWebSocket> peers = rooms.getOrDefault(roomId, Set.of());
    for (ServerWebSocket peer : peers) {
      if (peer != sender && !peer.isClosed()) {
        peer.writeTextMessage(outbound.encode());
      }
    }
  }

  private void leave(String roomId, ServerWebSocket socket) {
    Set<ServerWebSocket> peers = rooms.get(roomId);
    if (peers == null) {
      return;
    }
    peers.remove(socket);
    if (peers.isEmpty()) {
      rooms.remove(roomId, peers);
    }
  }
}
