package com.example.procurement;

import com.example.procurement.ApiModels.AnalysisResponse;
import com.example.procurement.ApiModels.AnalyzeRequest;
import com.example.procurement.ApiModels.ApproveRequest;
import com.example.procurement.ApiModels.RequestDetail;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/procurement")
public class ProcurementController {

  private final ProcurementService service;

  public ProcurementController(ProcurementService service) {
    this.service = service;
  }

  @PostMapping("/analyze")
  public AnalysisResponse analyze(
      @RequestHeader(value = "X-Role", defaultValue = "purchaser") String role,
      @Valid @RequestBody AnalyzeRequest request) {
    return service.analyze(request.text(), role);
  }

  @GetMapping("/requests")
  public List<PurchaseRequest> requests() {
    return service.listRequests();
  }

  @GetMapping("/requests/{id}")
  public RequestDetail detail(@PathVariable Long id) {
    return service.detail(id);
  }

  @PostMapping("/requests/{id}/approve")
  public RequestDetail approve(
      @PathVariable Long id,
      @RequestHeader(value = "X-Role", defaultValue = "purchaser") String role,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @Valid @RequestBody ApproveRequest request) {
    return service.decide(
        id, request.approver(), request.action(), request.note(), role, idempotencyKey);
  }

  @GetMapping("/requests/{id}/audit")
  public List<AuditLog> audit(@PathVariable Long id) {
    return service.audit(id);
  }
}
