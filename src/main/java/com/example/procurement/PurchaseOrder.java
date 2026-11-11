package com.example.procurement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseOrder {
  public Long id;
  public String poNo;
  public Long requestId;
  public String supplier;
  public BigDecimal total;
  public String status;
  public LocalDateTime createdAt;
}
