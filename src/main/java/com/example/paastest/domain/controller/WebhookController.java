package com.example.paastest.domain.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class WebhookController {
    @PostMapping("/inbound/orders")
    public ResponseEntity<String> customerOders(@RequestBody JsonNode payload){
        // 1. webhook_id 존재 여부 검사
        if (!payload.has("webhook_id") || payload.path("webhook_id").asText().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body("webhook_id가 없습니다.");
        }

        // 2. event_type 존재 여부 검사
        if (!payload.has("event_type") || payload.path("event_type").asText().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body("event_type이 없습니다.");
        }

        // 3. event_type 값 검사
        String eventType = payload.path("event_type").asText();

        if (!eventType.equals("order.created")) {
            return ResponseEntity
                    .badRequest()
                    .body("지원하지 않는 event_type입니다.");
        }


        // 유효성 검사 통과
        return ResponseEntity.ok("Webhook received successfully");
    }






    //json 데이터 용량 을 컨트롤러에 들어오기 전에 확인해서 대용량이면 못들어오게 처리
    //json 안의 webhook_id 가 있는 지 체크, event_type이 뭔지 체크
    //service단의 로직으로 넘긴다.


    // service 단에선 data 안의 order_id와 customer_id가 있는지 확인
    // 레거시에 보낼 포맷으로 변환을 진행 하면서 qty가 음수는 아닌지, price가 음수가 아닌지 체크
    // ERP응답시간이 2초 이상일 경우 time out에 걸리게 되고 우리 서버의 로그에는 남기고 클라이언트에는 200ok를 응답함.

    //트래픽 이 많을 시에는 kafka를 통해서 트래픽을 대기 시켜 하나씩 실행 시킨다.




}
