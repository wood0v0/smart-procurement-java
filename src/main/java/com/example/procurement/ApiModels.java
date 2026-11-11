package com.example.procurement;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public final class ApiModels {

  private ApiModels() {}

  public record AnalyzeRequest(@NotBlank(message = "需求描述不能为空") String text) {}

  public record ApproveRequest(
      @NotBlank(message = "审批人不能为空") String approver,
      @NotBlank(message = "审批动作不能为空") String action,
      String note) {}

  public record Candidate(
      Long productId,
      String brand,
      String model,
      String supplier,
      BigDecimal unitPrice,
      int quantity,
      BigDecimal shipping,
      BigDecimal total,
      double score,
      boolean withinBudget,
      int deliveryDays,
      String source) {}

  public record ItemCandidates(RequestItem selected, List<Candidate> candidates) {}

  public record AnalysisResponse(PurchaseRequest request, List<ItemCandidates> items) {}

  public record RequestDetail(
      PurchaseRequest request,
      List<RequestItem> items,
      List<Approval> approvals,
      PurchaseOrder order) {}
}
