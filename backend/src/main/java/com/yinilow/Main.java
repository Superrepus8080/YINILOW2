package com.yinilow;

import com.yinilow.api.MainVerticle;
import io.vertx.core.Vertx;

public final class Main {
  private Main() {
  }

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }
}
