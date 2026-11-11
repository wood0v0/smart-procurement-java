package com.example.procurement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProcurementApiTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void analyzesDemandAndApprovesToPurchaseOrder() throws Exception {
    MvcResult analyzed =
        mvc.perform(
                post("/api/v1/procurement/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"text\":\"采购 2 台联想笔记本，预算 10000\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.request.status").value("PENDING"))
            .andExpect(jsonPath("$.items[0].selected.brand").value("联想"))
            .andReturn();

    JsonNode body = objectMapper.readTree(analyzed.getResponse().getContentAsString());
    long requestId = body.path("request").path("id").asLong();
    String prNo = body.path("request").path("prNo").asText();

    mvc.perform(
            post("/api/v1/procurement/requests/" + requestId + "/approve")
                .header("X-Role", "approver")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"approver\":\"王经理\",\"action\":\"APPROVE\",\"note\":\"同意\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.request.status").value("APPROVED"))
        .andExpect(jsonPath("$.request.prNo").value(prNo))
        .andExpect(jsonPath("$.order.poNo").isNotEmpty())
        .andExpect(jsonPath("$.approvals[0].action").value("APPROVE"));

    mvc.perform(get("/api/v1/procurement/requests"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].prNo").value(prNo));
  }

  @Test
  void purchaserCannotApprove() throws Exception {
    MvcResult analyzed =
        mvc.perform(
                post("/api/v1/procurement/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"text\":\"采购 1 台显示器\"}"))
            .andExpect(status().isOk())
            .andReturn();

    long requestId =
        objectMapper.readTree(analyzed.getResponse().getContentAsString()).path("request").path("id").asLong();

    mvc.perform(
            post("/api/v1/procurement/requests/" + requestId + "/approve")
                .header("X-Role", "purchaser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"approver\":\"采购员\",\"action\":\"APPROVE\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void exposesOpenApiDocument() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("Procurement Backend API"))
        .andExpect(jsonPath("$.info.version").value("v1"))
        .andExpect(jsonPath("$.paths['/api/v1/procurement/analyze']").exists());
  }
}
