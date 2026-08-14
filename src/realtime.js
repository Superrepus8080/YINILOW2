const API_BASE = import.meta.env.VITE_YINILOW_API_BASE ?? "";

export function createRealtimeDataChannel({ roomId, initiator = false, onMessage, onStateChange } = {}) {
  if (!roomId) {
    throw new Error("REALTIME_ROOM_REQUIRED");
  }
  if (!("RTCPeerConnection" in window) || !("WebSocket" in window)) {
    throw new Error("REALTIME_UNAVAILABLE");
  }

  const peer = new RTCPeerConnection({
    iceServers: [{ urls: "stun:stun.l.google.com:19302" }],
  });
  const socket = new WebSocket(signalingUrl(roomId));
  let channel = null;

  function setState(state) {
    onStateChange?.(state);
  }

  function attachChannel(nextChannel) {
    channel = nextChannel;
    channel.binaryType = "arraybuffer";
    channel.onopen = () => setState("open");
    channel.onclose = () => setState("closed");
    channel.onerror = () => setState("error");
    channel.onmessage = (event) => {
      onMessage?.(decodePayload(event.data));
    };
  }

  if (initiator) {
    attachChannel(peer.createDataChannel("yinilow.telemetry", {
      ordered: false,
      maxRetransmits: 0,
    }));
  } else {
    peer.ondatachannel = (event) => attachChannel(event.channel);
  }

  peer.onicecandidate = (event) => {
    if (event.candidate) {
      sendSignal(socket, { type: "ice", candidate: event.candidate });
    }
  };

  socket.onopen = async () => {
    setState("signaling");
    sendSignal(socket, { type: "ready" });
    if (initiator) {
      const offer = await peer.createOffer();
      await peer.setLocalDescription(offer);
      sendSignal(socket, { type: "offer", description: peer.localDescription });
    }
  };

  socket.onmessage = async (event) => {
    const signal = JSON.parse(event.data);
    if (signal.type === "offer") {
      await peer.setRemoteDescription(signal.description);
      const answer = await peer.createAnswer();
      await peer.setLocalDescription(answer);
      sendSignal(socket, { type: "answer", description: peer.localDescription });
    }
    if (signal.type === "answer") {
      await peer.setRemoteDescription(signal.description);
    }
    if (signal.type === "ice" && signal.candidate) {
      await peer.addIceCandidate(signal.candidate);
    }
  };

  socket.onclose = () => setState(channel?.readyState === "open" ? "open" : "closed");
  socket.onerror = () => setState("error");

  return {
    sendTelemetry(payload) {
      if (channel?.readyState !== "open") {
        return false;
      }
      channel.send(JSON.stringify({
        type: "telemetry",
        sentAt: Date.now(),
        payload,
      }));
      return true;
    },
    close() {
      channel?.close();
      peer.close();
      socket.close();
    },
  };
}

function signalingUrl(roomId) {
  const base = API_BASE || window.location.origin;
  const url = new URL(base, window.location.origin);
  url.protocol = url.protocol === "https:" ? "wss:" : "ws:";
  url.pathname = `/api/v1/realtime/signaling/${encodeURIComponent(roomId)}`;
  url.search = "";
  return url.toString();
}

function sendSignal(socket, payload) {
  if (socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(payload));
  }
}

function decodePayload(data) {
  if (typeof data !== "string") {
    return data;
  }
  try {
    return JSON.parse(data);
  } catch {
    return { type: "raw", payload: data };
  }
}
