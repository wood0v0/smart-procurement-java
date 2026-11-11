package com.example.procurement;

import java.time.LocalDateTime;

public class AuditLog {
  public Long id;
  public Long requestId;
  public String action;
  public String actor;
  public String detail;
  public LocalDateTime createdAt;
}
