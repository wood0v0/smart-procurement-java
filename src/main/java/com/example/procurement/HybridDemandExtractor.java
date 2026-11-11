package com.example.procurement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Uses DeepSeek to extract structured demand when enabled, and falls back to the deterministic
 * rule parser if the model is unavailable or returns invalid data.
 */
@Component
public class HybridDemandExtractor implements DemandExtractor {

  private static final Logger log = LoggerFactory.getLogger(HybridDemandExtractor.class);
  private static final Set<String> KINDS =
      Set.of("laptop", "monitor", "tablet", "tool", "part", "consumable", "safety");

  private static final String SYSTEM_PROMPT =
      """
      你是采购需求解析器。只输出 JSON，不要解释，格式：
      {"items":[{"kind":"laptop|monitor|tablet|tool|part|consumable|safety",
      "brand":string|null,"model":string|null,"quantity":int,
      "budget":number|null,"deliveryDays":int}]}
      金额单位为人民币。无法确定的字段用 null；数量缺失时 quantity 用 1。
      """;

  private final DemandParser rules;
  private final ObjectMapper objectMapper;
  private final LlmProperties properties;
  private final RestClient client;

  public HybridDemandExtractor(
      DemandParser rules, ObjectMapper objectMapper, LlmProperties properties) {
    this.rules = rules;
    this.objectMapper = objectMapper;
    this.properties = properties;
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(properties.timeoutSeconds()));
    factory.setReadTimeout(Duration.ofSeconds(properties.timeoutSeconds()));
    this.client = RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(factory).build();
  }

  @Override
  public Demand extract(String text) {
    if (!"deepseek".equalsIgnoreCase(properties.mode()) || properties.apiKey().isBlank()) {
      return rules.parse(text);
    }
    try {
      Demand demand = extractWithModel(text);
      if (demand.items().isEmpty()) {
        throw new IllegalStateException("model returned no items");
      }
      return demand;
    } catch (Exception exception) {
      log.warn("LLM demand extraction failed, falling back to rules: {}", exception.getMessage());
      return rules.parse(text);
    }
  }

  private Demand extractWithModel(String text) throws Exception {
    Map<String, Object> body =
        Map.of(
            "model", properties.model(),
            "temperature", 0,
            "response_format", Map.of("type", "json_object"),
            "messages",
                List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", text)));

    JsonNode response =
        client
            .post()
            .uri("/chat/completions")
            .header("Authorization", "Bearer " + properties.apiKey())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(JsonNode.class);

    if (response == null) {
      throw new IllegalStateException("empty response");
    }
    String content = response.path("choices").path(0).path("message").path("content").asText("");
    content = content.replace("```json", "").replace("```", "").trim();
    JsonNode payload = objectMapper.readTree(content);
    JsonNode itemsNode = payload.path("items");
    if (!itemsNode.isArray()) {
      throw new IllegalStateException("missing items array");
    }

    List<Demand.Item> items = new ArrayList<>();
    for (JsonNode item : itemsNode) {
      String kind = item.path("kind").asText("");
      int quantity = item.path("quantity").asInt(0);
      if (!KINDS.contains(kind) || quantity <= 0) {
        continue;
      }
      String brand = item.path("brand").isNull() ? null : item.path("brand").asText(null);
      String model = item.path("model").isNull() ? null : item.path("model").asText(null);
      BigDecimal budget =
          item.path("budget").isNumber() ? item.path("budget").decimalValue() : null;
      int delivery = item.path("deliveryDays").asInt(3);
      items.add(
          new Demand.Item(
              kind,
              brand == null || brand.isBlank() ? null : brand,
              model == null || model.isBlank() ? null : model,
              quantity,
              budget,
              delivery > 0 ? delivery : 3));
    }
    return new Demand(text, items);
  }
}
