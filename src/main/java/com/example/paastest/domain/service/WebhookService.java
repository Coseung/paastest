package com.example.paastest.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    public void processAndSendToErp(JsonNode payload) {
        ObjectNode erpRequest = objectMapper.createObjectNode();
        erpRequest.put("erp_ref_id", payload.path("deal_id").asText("UNKNOWN_ID"));

        String priceStr = payload.path("price").asText("0");
        try {
            erpRequest.put("amount_number", Integer.parseInt(priceStr));
        } catch (NumberFormatException e) {
            erpRequest.put("amount_number", 0);
        }

        JsonNode emailNode = payload.get("client_email");
        if (emailNode == null || emailNode.isNull() || emailNode.asText().isEmpty()) {
            erpRequest.put("contact", "UNKNOWN");
        } else {
            erpRequest.put("contact", emailNode.asText());
        }
        erpRequest.put("sync_status", "COMPLETED");


        String erpUrl = "https://webhook.site/31c8a4c5-9f0a-4264-81f5-ee31acdec4e3";

        log.info("[ERP 전송 시작] 대상 ID: {}", erpRequest.get("erp_ref_id").asText());

        String targetId = erpRequest.path("erp_ref_id").asText();

        try{
            restTemplate.postForEntity(erpUrl, erpRequest, String.class);
            log.info("[ERP 전송 성공] 대상 ID: {}",targetId);
        }catch(ResourceAccessException e){
            log.error("[ERP 타임 아웃] 대상 ID: {}", targetId);
            throw e;
        }catch(RestClientResponseException e){
            log.error("[ERP 전송 실패] 대상 ID: {}, 상태 코드: {}, 응답: {}",
                    targetId, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }catch(Exception e){
            // 그 외 알 수 없는 시스템 에러
            log.error("[ERP 전송 치명적 오류] 대상 ID: {}, 원인: {}", targetId, e.getMessage());
            throw new RuntimeException("ERP Sync Internal Error", e);
        }

        log.info("[ERP 전송 성공]");
    }
}

