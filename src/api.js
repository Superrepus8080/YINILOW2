const API_BASE = import.meta.env.VITE_YINILOW_API_BASE ?? "";

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

export function getCart() {
  return request("/api/v1/cart");
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

export function createOrder(deliveryAddress) {
  return request("/api/v1/checkout/orders", {
    method: "POST",
    headers: {
      "x-idempotency-key": `order-${Date.now()}`,
    },
    body: JSON.stringify({
      deliveryAddress,
      paymentMethod: "MOMO_PLACEHOLDER",
    }),
  });
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
