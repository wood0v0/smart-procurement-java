package com.example.procurement;

import java.math.BigDecimal;
import java.util.List;

public record Demand(String rawText, List<Item> items) {

  public record Item(
      String kind,
      String brand,
      String model,
      int quantity,
      BigDecimal budget,
      int deliveryDays) {}
}
