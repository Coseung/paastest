package com.example.paastest.domain.controller.test2;

import com.example.paastest.domain.service.test2.ErpAsyncService;
import com.example.paastest.global.exception.NodeExecutionException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/erp")
public class WebhookERPController {

    private final ErpAsyncService erpAsyncService;
    @PostMapping("/ShipmentDetails-Insert")
    public ResponseEntity<String> ShipmentDetailsInsert(@RequestBody JsonNode json){
        //transactionId와 partnerId를 검사하는 로직입니다.
        // 유효성 검사를 controller단에서 한 이유는 데이터 값이 service단 까지 가는것을 막기 위해 controller에서 작성하였습니다.
        JsonNode getTransctionId = json.path("transactionId");
        JsonNode getPartnerId = json.path("partnerId");
        if(getTransctionId.isNull() || getTransctionId.asText().isBlank()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("transactionId가 들어있지않습니다.");
        }
        if(getPartnerId.isNull() || getPartnerId.asText().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("partnerId가 존재하지않습니다.");
        }

        erpAsyncService.ErpAsyncRequest(json);

        return ResponseEntity.accepted().body("요청이 접수되었습니다.");
    }




}
