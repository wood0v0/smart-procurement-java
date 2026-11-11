package com.example.procurement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DemandParserTest {

  private final DemandParser parser = new DemandParser();

  @Test
  void parsesQuantityBrandBudgetAndKind() {
    Demand demand = parser.parse("采购 2 台联想笔记本，预算 10000");

    assertThat(demand.items()).hasSize(1);
    Demand.Item item = demand.items().get(0);
    assertThat(item.kind()).isEqualTo("laptop");
    assertThat(item.brand()).isEqualTo("联想");
    assertThat(item.quantity()).isEqualTo(2);
    assertThat(item.budget()).isEqualByComparingTo(new BigDecimal("10000"));
  }

  @Test
  void correctsCommonTypos() {
    Demand demand = parser.parse("买 3 台显视器");

    assertThat(demand.items().get(0).kind()).isEqualTo("monitor");
    assertThat(demand.items().get(0).quantity()).isEqualTo(3);
  }

  @Test
  void rejectsDemandWithoutQuantity() {
    assertThatThrownBy(() -> parser.parse("采购一些办公用品"))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("采购数量");
  }
}
