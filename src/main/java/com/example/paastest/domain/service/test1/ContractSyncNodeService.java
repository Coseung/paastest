package com.example.paastest.domain.service.test1;

import com.example.paastest.global.exception.NodeExecutionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractSyncNodeService {
    private final ObjectMapper objectMapper;
    private static final String NODE_NAME = "사내 S3 보관소 동기화 노드";
    private final ConcurrentHashMap<String, String> idempotencyMap = new ConcurrentHashMap<>();

    public void process(JsonNode payload) {
        log.info("[{}] 실행 시작", NODE_NAME);

        if(payload.get("event_type") ==null ||
                !payload.get("event_type").asText().equals("contract_completed")){
            return;
        }

        JsonNode getDocument = payload.path("document");
        String customerId =getDocument.path("customer_id").asText("").trim();
        String docId = getDocument.path("doc_id").asText("").trim();

        if(getDocument.isNull() ||
                getDocument.isMissingNode()){
            log.error("부모 노드 없음: {}",payload.asText());
            throw new NodeExecutionException(NODE_NAME,"부모 노드 없음",null);
        }
        if(customerId.isBlank() || docId.isBlank()){
            log.error("customer_id 와 doc_id둘중 하나 없음: customer_id: {}, doc_id: {}",
                    customerId,
                    docId);
            throw new NodeExecutionException(NODE_NAME,"필드 중 하나가 없음",null);
        }


        // 4. 검증을 통과했다면 조립
        String s3ObjectKey = customerId + "_" + docId;

        log.info("[{}] S3 키 조립 완료: {}", NODE_NAME, s3ObjectKey);


        String previousState = idempotencyMap.putIfAbsent(s3ObjectKey,"PROCESSING");

        if(previousState != null){
            if(previousState.equals("SUCCESS")){
                log.info("[{}] 이미 성공적으로 동기화된 데이터입니다. (Key: {})", NODE_NAME, s3ObjectKey);
                return;

            }else{
                log.warn("[{}] 현재 처리 중이거나 이전 처리가 실패한 중복 요청입니다. (상태: {})", NODE_NAME, previousState);
                throw new NodeExecutionException(NODE_NAME, "중복 요청 방어: " + previousState, null);
            }

        }

            try {
                log.info("[{}전송 시작] ",NODE_NAME);
                //s3로직 전송
                idempotencyMap.put(s3ObjectKey,"SUCCESS");

            }catch (ResourceAccessException e){
                log.error("[{} 타임 아웃] 대상 json: {}", NODE_NAME, payload);
                idempotencyMap.put(s3ObjectKey,"FAILED");
                throw new NodeExecutionException(NODE_NAME, "S3 서버 타임아웃",e);

            }catch (RestClientResponseException e) {
                log.error("[{} 전송 실패] 대상 json: {}, 상태 코드: {}, 응답: {}",
                    NODE_NAME, payload, e.getStatusCode(), e.getResponseBodyAsString());
                idempotencyMap.put(s3ObjectKey,"FAILED");
                throw new NodeExecutionException(NODE_NAME, "S3 데이터 전송 실패",e);
            }
            catch (Exception e) {
                log.error("[{}] 실행 중 치명적 오류 발생", NODE_NAME);
                idempotencyMap.put(s3ObjectKey,"FAILED");
                throw new NodeExecutionException(NODE_NAME, "S3 연동 실패", e);
            }



    }
}
