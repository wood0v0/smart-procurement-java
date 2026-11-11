package com.example.procurement;

import java.time.LocalDateTime;

public class Approval {
  public Long id;
  public Long requestId;
  public String approver;
  public String action;
  public String note;
  public LocalDateTime createdAt;
}
