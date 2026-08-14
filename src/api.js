const API_BASE = import.meta.env.VITE_YINILOW_API_BASE ?? "";
const SELLER_SESSION_KEY = "yinilow.sellerSession";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "content-type": "application/json",
      ...(options.headers ?? {}),
    },
    ...options,
  });

  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    const error = new Error(payload.error ?? "REQUEST_FAILED");
    error.status = response.status;
    error.payload = payload;
    throw error;
  }
  return payload;
}

export function getListings() {
  return request("/api/v1/catalog/listings");
}

export function getListing(listingId) {
  return request(`/api/v1/catalog/listings/${listingId}`);
}

export function createAdminListing(listing) {
  return request("/api/v1/admin/catalog/listings", {
    method: "POST",
    headers: sellerHeaders(),
    body: JSON.stringify(listing),
  });
}

export function getAdminListings() {
  return request("/api/v1/admin/catalog/listings", { headers: sellerHeaders() });
}

export function updateAdminListingVisibility(listingId, visibility) {
  return request(`/api/v1/admin/catalog/listings/${listingId}/visibility`, {
    method: "PATCH",
    headers: sellerHeaders(),
    body: JSON.stringify({ visibility }),
  });
}

export function getStoredSellerSession() {
  try {
    return JSON.parse(window.localStorage.getItem(SELLER_SESSION_KEY) || "null");
  } catch {
    return null;
  }
}

export function storeSellerSession(session) {
  window.localStorage.setItem(SELLER_SESSION_KEY, JSON.stringify(session));
}

export function clearSellerSession() {
  window.localStorage.removeItem(SELLER_SESSION_KEY);
}

export async function sellerLogin(credentials) {
  const session = await request("/api/v1/seller/sessions", {
    method: "POST",
    body: JSON.stringify(credentials),
  });
  storeSellerSession(session);
  return session;
}

export async function createSellerAccount(account) {
  const session = await request("/api/v1/seller/accounts", {
    method: "POST",
    body: JSON.stringify(account),
  });
  storeSellerSession(session);
  return session;
}

export function sellerLogout() {
  const session = getStoredSellerSession();
  clearSellerSession();
  if (!session?.token) {
    return Promise.resolve();
  }
  return request("/api/v1/seller/session", {
    method: "DELETE",
    headers: { authorization: `Bearer ${session.token}` },
  }).catch(() => {});
}

function sellerHeaders() {
  const session = getStoredSellerSession();
  return session?.token ? { authorization: `Bearer ${session.token}` } : {};
}

export function getCart() {
  return request("/api/v1/cart");
}

export function recordRealtimeTelemetry(event) {
  return request("/api/v1/realtime/telemetry", {
    method: "POST",
    body: JSON.stringify(event),
  }).catch(() => null);
}

export function addToBag(listingId) {
  return request("/api/v1/cart/items", {
    method: "POST",
    headers: {
      "x-idempotency-key": `add-${listingId}-${Date.now()}`,
    },
    body: JSON.stringify({
      listingId,
      quantity: 1,
      source: "PRODUCT_DETAIL",
    }),
  });
}

export function checkoutQuote() {
  return request("/api/v1/checkout/quote", { method: "POST", body: "{}" });
}

export function createOrder(deliveryAddress, paymentMethod = "MOBILE_MONEY") {
  return request("/api/v1/checkout/orders", {
    method: "POST",
    headers: {
      "x-idempotency-key": `order-${Date.now()}`,
    },
    body: JSON.stringify({
      deliveryAddress,
      paymentMethod,
    }),
  });
}

export function getOrder(orderNumber, phone) {
  const params = new URLSearchParams({ phone });
  return request(`/api/v1/orders/${encodeURIComponent(orderNumber)}?${params.toString()}`);
}

export function initializePayment(orderId) {
  return request("/api/v1/payments/initialize", {
    method: "POST",
    headers: {
      "x-idempotency-key": `payment-${orderId}`,
    },
    body: JSON.stringify({
      orderId,
      provider: "SANDBOX",
    }),
  });
}

export function confirmSandboxPayment(providerReference) {
  return request("/api/v1/payments/callbacks/sandbox", {
    method: "POST",
    body: JSON.stringify({
      provider: "SANDBOX",
      providerReference,
      status: "success",
    }),
  });
}
