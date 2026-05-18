package com.example.paastest.domain.service.test1;


import com.example.paastest.global.exception.NodeExecutionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNodeService {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String NODE_NAME = "Slack 사내 알림 노드";
    private final String SLACK_URL = "https://webhook.site/31c8a4c5-9f0a-4264-81f5-ee31acdec4e3";

    public void process(JsonNode payload) {
        try {
            log.info("[{}] 실행 시작", NODE_NAME);


            // TODO: 여기서 슬랙 규격에 맞는 ObjectNode 조합 (text: "신규 주문 발생: ...")
            // TODO: RestTemplate을 이용한 POST 전송
            String itemName = payload.path("item_name").asText("UNKNOWN_ITEM");
            String amount = payload.path("amount").asText("0");
            ObjectNode requestSlack = objectMapper.createObjectNode();
            requestSlack.put("text", "신규 주문 발생 " + itemName + " (" + amount + "원)");
            log.info("[request Slack 할당] itemName: {}, amount: {} ", itemName, amount);

            restTemplate.postForEntity(SLACK_URL, requestSlack, String.class);
            log.info("[{}] 실행 성공", NODE_NAME);

            } catch (ResourceAccessException e) {
                    log.error("[{} 타임 아웃] 대상 json: {}", NODE_NAME, payload);
                    throw new NodeExecutionException(NODE_NAME, "Slack 서버 타임아웃",e);
            } catch (RestClientResponseException e) {
                    log.error("[{} 전송 실패] 대상 json: {}, 상태 코드: {}, 응답: {}",
                            NODE_NAME, payload, e.getStatusCode(), e.getResponseBodyAsString());
                    throw new NodeExecutionException(NODE_NAME, "Slack 데이터 전송 실패",e);
            }
            catch (Exception e) {
                log.error("[{}] 실행 중 치명적 오류 발생", NODE_NAME);
                throw new NodeExecutionException(NODE_NAME, "Slack 연동 실패", e);
            }
        }
    }
