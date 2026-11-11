package com.example.procurement;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(String mode, String baseUrl, String apiKey, String model, int timeoutSeconds) {

  public LlmProperties {
    mode = mode == null || mode.isBlank() ? "rules" : mode;
    baseUrl = baseUrl == null || baseUrl.isBlank() ? "https://api.deepseek.com" : baseUrl;
    apiKey = apiKey == null ? "" : apiKey;
    model = model == null || model.isBlank() ? "deepseek-chat" : model;
    timeoutSeconds = timeoutSeconds <= 0 ? 20 : timeoutSeconds;
  }
}
