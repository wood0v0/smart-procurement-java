package com.example.procurement;

import java.math.BigDecimal;
import java.time.LocalDate;

public class QuoteView {
  public Long productId;
  public String kind;
  public String brand;
  public String model;
  public String supplier;
  public BigDecimal rating;
  public BigDecimal shippingFee;
  public BigDecimal unitPrice;
  public int stock;
  public int deliveryDays;
  public String source;
  public LocalDate validUntil;
}
