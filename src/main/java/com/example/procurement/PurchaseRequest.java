package com.example.procurement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseRequest {
  public Long id;
  public String prNo;
  public String rawText;
  public String status;
  public BigDecimal total;
  public LocalDateTime createdAt;
}
