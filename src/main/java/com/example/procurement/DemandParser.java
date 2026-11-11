package com.example.procurement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class DemandParser {

  private static final Map<String, String> CORRECTIONS =
      Map.of(
          "笔迹本", "笔记本",
          "显视器", "显示器",
          "平版", "平板",
          "台示器", "台式机",
          "按全帽", "安全帽");

  private static final List<Map.Entry<String, String>> KINDS =
      List.of(
          Map.entry("安全帽|手套|护目镜|口罩|劳保鞋|防护", "safety"),
          Map.entry("轴承|螺栓|螺母|垫片|皮带|齿轮|气缸|继电器|传感器|零件|备件", "part"),
          Map.entry("焊条|砂纸|切削液|扎带|生料带|润滑油|耗材", "consumable"),
          Map.entry("扳手|螺丝刀|电钻|钳|锤|卷尺|卡尺|角磨机|工具", "tool"),
          Map.entry("ipad|平板|matepad|tablet", "tablet"),
          Map.entry("显示器|屏幕|monitor", "monitor"),
          Map.entry("笔记本|电脑|laptop|thinkpad|macbook", "laptop"));

  private static final List<String> BRANDS =
      List.of("联想", "戴尔", "惠普", "华为", "小米", "苹果", "AOC", "LG", "华硕", "宏碁", "世达", "霍尼韦尔");

  private static final Pattern QUANTITY = Pattern.compile("(\\d+)\\s*(台|个|件|套|只|箱|包|瓶|桶|把|支|根|卷|双|副)");
  private static final Pattern BUDGET = Pattern.compile("预算\\s*([0-9,]+)|([0-9,]+)\\s*元");
  private static final Pattern DELIVERY = Pattern.compile("(\\d+)\\s*天");

  public Demand parse(String rawText) {
    String text = normalize(rawText);
    BigDecimal globalBudget = findBudget(text);
    int globalDelivery = findDelivery(text);

    List<Demand.Item> items = new ArrayList<>();
    for (String segment : text.split("[、,，;；]|和")) {
      int quantity = findQuantity(segment);
      if (quantity == 0) {
        continue;
      }
      String kind = findKind(segment);
      if (kind == null) {
        throw new ApiException(400, "无法识别需求中的品类：" + segment.trim());
      }
      String brand = findBrand(segment);
      items.add(
          new Demand.Item(
              kind,
              brand,
              null,
              quantity,
              globalBudget,
              globalDelivery > 0 ? globalDelivery : 3));
    }

    if (items.isEmpty()) {
      throw new ApiException(400, "没有识别到采购数量和品类，请补充类似「2 台笔记本」的描述");
    }
    return new Demand(rawText, items);
  }

  private String normalize(String text) {
    String result = text == null ? "" : text.trim();
    for (Map.Entry<String, String> entry : CORRECTIONS.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }

  private int findQuantity(String text) {
    Matcher matcher = QUANTITY.matcher(text);
    return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
  }

  private BigDecimal findBudget(String text) {
    Matcher matcher = BUDGET.matcher(text);
    if (!matcher.find()) {
      return null;
    }
    String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    return new BigDecimal(value.replace(",", ""));
  }

  private int findDelivery(String text) {
    Matcher matcher = DELIVERY.matcher(text);
    return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
  }

  private String findKind(String text) {
    String lower = text.toLowerCase();
    for (Map.Entry<String, String> entry : KINDS) {
      if (Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
        return entry.getValue();
      }
    }
    return null;
  }

  private String findBrand(String text) {
    return BRANDS.stream().filter(text::contains).findFirst().orElse(null);
  }
}
