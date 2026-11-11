package com.example.procurement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcurementService {

  private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

  private final DemandExtractor demandExtractor;
  private final CatalogMapper catalog;
  private final RequestMapper requests;

  public ProcurementService(
      DemandExtractor demandExtractor, CatalogMapper catalog, RequestMapper requests) {
    this.demandExtractor = demandExtractor;
    this.catalog = catalog;
    this.requests = requests;
  }

  @Transactional
  public ApiModels.AnalysisResponse analyze(String text) {
    Demand demand = demandExtractor.extract(text);
    List<ApiModels.ItemCandidates> results = new ArrayList<>();
    BigDecimal requestTotal = BigDecimal.ZERO;

    for (Demand.Item item : demand.items()) {
      List<QuoteView> quotes = catalog.findValidQuotesByKind(item.kind());
      if (item.brand() != null) {
        quotes = quotes.stream().filter(q -> item.brand().equalsIgnoreCase(q.brand)).toList();
      }
      if (quotes.isEmpty()) {
        throw new ApiException(400, "没有找到匹配的报价：" + item.kind()
            + (item.brand() == null ? "" : " / " + item.brand()));
      }

      List<ApiModels.Candidate> candidates = toCandidates(quotes, item);
      ApiModels.Candidate best = pickBest(candidates, item.budget());
      requestTotal = requestTotal.add(best.total());

      RequestItem selected = new RequestItem();
      selected.productId = best.productId();
      selected.kind = item.kind();
      selected.brand = best.brand();
      selected.model = best.model();
      selected.quantity = item.quantity();
      selected.unitPrice = best.unitPrice();
      selected.supplier = best.supplier();
      selected.total = best.total();
      results.add(new ApiModels.ItemCandidates(selected, candidates));
    }

    PurchaseRequest request = new PurchaseRequest();
    request.rawText = text;
    request.status = "PENDING";
    request.total = requestTotal;
    requests.insertRequest(request);
    request.prNo =
        "PR-" + LocalDate.now().format(DATE) + "-" + String.format(Locale.ROOT, "%04d", request.id);
    requests.updatePrNo(request.id, request.prNo);

    for (ApiModels.ItemCandidates result : results) {
      result.selected().requestId = request.id;
      requests.insertItem(result.selected());
    }
    return new ApiModels.AnalysisResponse(request, results);
  }

  public List<PurchaseRequest> listRequests() {
    return requests.findAllRequests();
  }

  public ApiModels.RequestDetail detail(Long id) {
    PurchaseRequest request = requests.findRequestById(id);
    if (request == null) {
      throw new ApiException(404, "采购申请不存在");
    }
    return new ApiModels.RequestDetail(
        request,
        requests.findItemsByRequest(id),
        requests.findApprovalsByRequest(id),
        requests.findOrderByRequest(id));
  }

  @Transactional
  public ApiModels.RequestDetail decide(Long id, String approver, String action, String note, String role) {
    if (!"approver".equals(role) && !"director".equals(role)) {
      throw new ApiException(403, "只有审批人可以审批");
    }
    PurchaseRequest request = requests.findRequestById(id);
    if (request == null) {
      throw new ApiException(404, "采购申请不存在");
    }
    if (!"PENDING".equals(request.status)) {
      throw new ApiException(409, "该申请已处理，不能重复审批");
    }

    String normalized = action.trim().toUpperCase(Locale.ROOT);
    if (!"APPROVE".equals(normalized) && !"REJECT".equals(normalized)) {
      throw new ApiException(400, "审批动作只支持 APPROVE 或 REJECT");
    }

    Approval approval = new Approval();
    approval.requestId = id;
    approval.approver = approver;
    approval.action = normalized;
    approval.note = note;
    requests.insertApproval(approval);
    requests.updateStatus(id, "APPROVE".equals(normalized) ? "APPROVED" : "REJECTED");

    if ("APPROVE".equals(normalized)) {
      List<RequestItem> items = requests.findItemsByRequest(id);
      PurchaseOrder order = new PurchaseOrder();
      order.poNo =
          "PO-" + LocalDate.now().format(DATE) + "-" + String.format(Locale.ROOT, "%04d", id);
      order.requestId = id;
      order.supplier = items.stream().map(i -> i.supplier).distinct().reduce((a, b) -> a + " / " + b).orElse("");
      order.total = request.total;
      order.status = "CREATED";
      requests.insertOrder(order);
    }
    return detail(id);
  }

  private List<ApiModels.Candidate> toCandidates(List<QuoteView> quotes, Demand.Item item) {
    BigDecimal minTotal =
        quotes.stream()
            .map(q -> total(q, item.quantity()))
            .min(Comparator.naturalOrder())
            .orElse(BigDecimal.ZERO);

    return quotes.stream()
        .map(
            quote -> {
              BigDecimal shipping = quote.shippingFee == null ? BigDecimal.ZERO : quote.shippingFee;
              BigDecimal total = total(quote, item.quantity());
              boolean withinBudget =
                  item.budget() == null || total.compareTo(item.budget()) <= 0;
              double priceScore =
                  total.signum() == 0
                      ? 0
                      : minTotal.doubleValue() / total.doubleValue() * 35;
              double budgetScore = withinBudget ? 20 : 0;
              double deliveryScore =
                  Math.max(0, 15 - quote.deliveryDays) / 15.0 * 10;
              double ratingScore =
                  (quote.rating == null ? 0 : quote.rating.doubleValue()) / 5.0 * 10;
              double stockScore = quote.stock >= item.quantity() ? 5 : (quote.stock > 0 ? 2 : 0);
              double score = Math.round((priceScore + budgetScore + deliveryScore + ratingScore + stockScore) * 10.0) / 10.0;
              return new ApiModels.Candidate(
                  quote.productId,
                  quote.brand,
                  quote.model,
                  quote.supplier,
                  quote.unitPrice,
                  item.quantity(),
                  shipping,
                  total,
                  score,
                  withinBudget,
                  quote.deliveryDays,
                  quote.source);
            })
        .sorted(Comparator.comparingDouble(ApiModels.Candidate::score).reversed())
        .toList();
  }

  private ApiModels.Candidate pickBest(List<ApiModels.Candidate> candidates, BigDecimal budget) {
    boolean allOverBudget =
        budget != null && candidates.stream().noneMatch(ApiModels.Candidate::withinBudget);
    if (allOverBudget) {
      return candidates.stream()
          .min(Comparator.comparing(ApiModels.Candidate::total))
          .orElseThrow();
    }
    return candidates.get(0);
  }

  private BigDecimal total(QuoteView quote, int quantity) {
    BigDecimal shipping = quote.shippingFee == null ? BigDecimal.ZERO : quote.shippingFee;
    return quote.unitPrice
        .multiply(BigDecimal.valueOf(quantity))
        .add(shipping)
        .setScale(2, RoundingMode.HALF_UP);
  }
}
