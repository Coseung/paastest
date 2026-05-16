package com.example.paastest.domain.service;

import com.example.paastest.global.exception.NodeExecutionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractSyncNodeService {
    private final ObjectMapper objectMapper;
    private static final String NODE_NAME = "사내 S3 보관소 동기화 노드";

    public void process(JsonNode payload) {
        log.info("[{}] 실행 시작", NODE_NAME);

        JsonNode getDocument = payload.get("document");
        if(payload.get("event_type") ==null ||
                !payload.get("event_type").asText().equals("contract_completed")){
            return;
        }
        if(getDocument ==null ||
                getDocument.isMissingNode()){
            log.error("부모 노드 없음: {}",payload.asText());
            throw new RuntimeException("부모 노드 없음");
        }
        if(getDocument.get("customer_id") ==null ||
                getDocument.get("customer_id").isNull() ||
                getDocument.get("customer_id").asText().isBlank() ||
                getDocument.get("doc_id") ==null ||
                getDocument.get("doc_id").isNull() ||
                getDocument.get("doc_id").asText().isBlank()
        ){
            log.error("customer_id 와 doc_id둘중 하나 없음: customer_id: {}, doc_id: {}",
                    getDocument.path("customer_id").asText(),
                    getDocument.path("doc_id").asText());
            throw new RuntimeException("필드 중 하나가 없음");
        }


        // 4. 검증을 통과했다면 조립
        String customerId = payload.path("document").path("customer_id").asText();
        String docId = payload.path("document").path("doc_id").asText();
        String s3ObjectKey = customerId + "_" + docId;

        log.info("[{}] S3 키 조립 완료: {}", NODE_NAME, s3ObjectKey);

        // (이후 S3 API 전송 로직이 있다고 가정합니다)
    }
}
