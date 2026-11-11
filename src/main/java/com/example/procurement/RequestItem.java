package com.example.procurement;

import java.math.BigDecimal;

public class RequestItem {
  public Long id;
  public Long requestId;
  public Long productId;
  public String kind;
  public String brand;
  public String model;
  public int quantity;
  public BigDecimal unitPrice;
  public String supplier;
  public BigDecimal total;
}
