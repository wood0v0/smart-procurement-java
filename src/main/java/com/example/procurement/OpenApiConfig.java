package com.example.procurement;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI procurementOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Procurement Backend API")
                .version("v1")
                .description("供应商比价、采购申请、审批与采购单 API"));
  }
}
