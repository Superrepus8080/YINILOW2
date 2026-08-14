package com.yinilow.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

class MemoryPaymentServiceTest {
  @Test
  void initializesPaymentIdempotently() {
    MemoryPaymentService service = new MemoryPaymentService();
    JsonObject request = new JsonObject().put("orderId", "ord_1");

    PaymentManager.PaymentResult first = service.initializePayment(request, "pay_1");
    PaymentManager.PaymentResult replay = service.initializePayment(request, "pay_1");

    assertTrue(first.success());
    assertEquals(first.payload().getString("providerReference"), replay.payload().getString("providerReference"));
  }

  @Test
  void confirmsSandboxCallback() {
    MemoryPaymentService service = new MemoryPaymentService();

    PaymentManager.PaymentResult result = service.confirmCallback(new JsonObject()
      .put("provider", "SANDBOX")
      .put("providerReference", "sandbox_ref"));

    assertEquals("CONFIRMED", result.payload().getString("status"));
    assertEquals("PAID", result.payload().getString("paymentStatus"));
  }
}
