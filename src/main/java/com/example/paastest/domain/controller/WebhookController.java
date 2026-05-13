package com.example.paastest.domain.controller;

import com.example.paastest.domain.service.WebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService webhookService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncDeal(@RequestBody JsonNode payload){
        JsonNode eventTypeNode = payload.get("event_type");
        if (eventTypeNode == null || eventTypeNode.isNull() || !eventTypeNode.asText().equals("deal_won")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이벤트 타입이 맞지않습니다");
        }
        webhookService.processAndSendToErp(payload);

        return ResponseEntity.ok("ERP Sync Completed");

    }

}
