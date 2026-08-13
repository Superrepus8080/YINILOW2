package com.yinilow.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class HoldServiceTest {
  @Test
  void allowsOnlyOneActiveHoldForOneOffItem() {
    HoldService service = new HoldService();

    HoldService.HoldResult first = service.createHold("unit_1", "listing_1", "cart_1");
    HoldService.HoldResult second = service.createHold("unit_1", "listing_1", "cart_2");

    assertTrue(first.created());
    assertEquals("ITEM_HELD", second.errorCode());
  }

  @Test
  void concurrentHoldsProduceOneWinner() throws Exception {
    HoldService service = new HoldService();
    CountDownLatch start = new CountDownLatch(1);
    List<HoldService.HoldResult> results = new ArrayList<>();
    var executor = Executors.newFixedThreadPool(2);

    for (int i = 0; i < 2; i++) {
      int cartNumber = i;
      executor.submit(() -> {
        try {
          start.await();
          synchronized (results) {
            results.add(service.createHold("unit_2", "listing_2", "cart_" + cartNumber));
          }
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
        }
      });
    }

    start.countDown();
    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

    long created = results.stream().filter(HoldService.HoldResult::created).count();
    long conflicts = results.stream().filter(result -> "ITEM_HELD".equals(result.errorCode())).count();
    assertEquals(1, created);
    assertEquals(1, conflicts);
  }
}
