package com.example.procurement;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class HybridDemandExtractorTest {

  private final DemandParser rules = new DemandParser();

  @Test
  void usesRulesWhenLlmIsDisabled() {
    HybridDemandExtractor extractor =
        new HybridDemandExtractor(
            rules,
            new ObjectMapper(),
            new LlmProperties("rules", "https://api.deepseek.com", "", "deepseek-chat", 5));

    Demand demand = extractor.extract("采购 2 台联想笔记本，预算 10000");

    assertThat(demand.items()).hasSize(1);
    assertThat(demand.items().get(0).kind()).isEqualTo("laptop");
    assertThat(demand.items().get(0).quantity()).isEqualTo(2);
  }

  @Test
  void fallsBackToRulesWhenApiKeyIsMissing() {
    HybridDemandExtractor extractor =
        new HybridDemandExtractor(
            rules,
            new ObjectMapper(),
            new LlmProperties("deepseek", "https://api.deepseek.com", "", "deepseek-chat", 5));

    Demand demand = extractor.extract("买 3 台显示器");

    assertThat(demand.items().get(0).kind()).isEqualTo("monitor");
    assertThat(demand.items().get(0).quantity()).isEqualTo(3);
  }
}
