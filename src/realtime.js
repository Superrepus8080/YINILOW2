const API_BASE = import.meta.env.VITE_YINILOW_API_BASE ?? "";

export function createRealtimeDataChannel({ roomId, onMessage, onPeerCount, onStateChange } = {}) {
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
  const queuedSignals = [];
  const pendingCandidates = [];
  let channel = null;
  let makingOffer = false;
  let peerId = "";

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

  function emitSignal(payload) {
    if (socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify(payload));
      return;
    }
    queuedSignals.push(payload);
  }

  attachChannel(peer.createDataChannel("yinilow.telemetry", {
    ordered: false,
    maxRetransmits: 0,
  }));
  peer.ondatachannel = (event) => attachChannel(event.channel);

  peer.onnegotiationneeded = async () => {
    try {
      makingOffer = true;
      await peer.setLocalDescription();
      emitSignal({ type: "offer", description: peer.localDescription });
    } finally {
      makingOffer = false;
    }
  };

  peer.onicecandidate = (event) => {
    if (event.candidate) {
      emitSignal({ type: "ice", candidate: event.candidate });
    }
  };

  socket.onopen = async () => {
    setState("signaling");
    emitSignal({ type: "ready" });
    for (const signal of queuedSignals.splice(0)) {
      emitSignal(signal);
    }
  };

  socket.onmessage = async (event) => {
    const signal = JSON.parse(event.data);
    if (signal.type === "joined") {
      peerId = signal.peerId;
      return;
    }
    if (signal.type === "peer-count") {
      onPeerCount?.(signal.count);
      return;
    }
    if (signal.type === "offer") {
      const polite = peerId && signal.fromPeerId ? peerId > signal.fromPeerId : true;
      const offerCollision = makingOffer || peer.signalingState !== "stable";
      if (!polite && offerCollision) {
        return;
      }
      if (offerCollision) {
        await Promise.all([
          peer.setLocalDescription({ type: "rollback" }),
          peer.setRemoteDescription(signal.description),
        ]);
      } else {
        await peer.setRemoteDescription(signal.description);
      }
      for (const candidate of pendingCandidates.splice(0)) {
        await peer.addIceCandidate(candidate);
      }
      await peer.setLocalDescription();
      emitSignal({ type: "answer", description: peer.localDescription });
      return;
    }
    if (signal.type === "answer" && peer.signalingState !== "stable") {
      await peer.setRemoteDescription(signal.description);
      for (const candidate of pendingCandidates.splice(0)) {
        await peer.addIceCandidate(candidate);
      }
      return;
    }
    if (signal.type === "ice" && signal.candidate) {
      if (peer.remoteDescription) {
        await peer.addIceCandidate(signal.candidate);
      } else {
        pendingCandidates.push(signal.candidate);
      }
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
