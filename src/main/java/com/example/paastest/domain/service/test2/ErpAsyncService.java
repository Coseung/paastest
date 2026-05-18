package com.example.paastest.domain.service.test2;

import com.example.paastest.domain.component.test2.ErpAsyncComponent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErpAsyncService {

    private final ErpAsyncComponent erpAsyncComponent;
    //ConcurrentHashMap 을 사용한 이유는 동시성을 막기 위함입니다. redis를 대신하여 사용하였습니다.
    //만약 redis라면 SET transactionId PROCESSING NX EX 300 를 사용해서 만료 시간을 정해줍니다.
    private final ConcurrentHashMap<String, String> transactionIdMap =
            new ConcurrentHashMap<>();

    public void ErpAsyncRequest(JsonNode payload) {
        //putIfAbsent를 사용하여 transactionid가  service에 도착한 흔적이있다면 멱등성을 위해 중복 요청 처리 되어 에러를 던집니다.
        String transactionState = transactionIdMap.putIfAbsent(payload.path("transactionId").asText(), "PROCESSING");
        JsonNode items = payload.path("items");
        String transactionId = payload.path("transactionId").asText();
        Set<String> duplicateIDset = new HashSet<>();
        if (transactionState != null) {
            throw new RuntimeException("중복 요청입니다.");
        }

        // item안의 중복 recordId, amount의 음수값, currency의 값체크 를 위한 for문을 작성하였습니다.
        for (JsonNode item : items) {
            String recordId = item.path("recordId").asText();
            int amount = item.path("amount").asInt();
            String currency = item.path("currency").asText();
            if (amount < 0) {
                transactionIdMap.put(transactionId, "FAILED");

                log.warn("[ERP_ASYNC] amount 값이 잘못되었습니다. transactionId={}, recordId={}, amount={}",
                        transactionId, recordId, amount);

                return;
            }

            if (!"KRW".equals(currency)) {
                transactionIdMap.put(transactionId, "FAILED");

                log.warn("[ERP_ASYNC] currency가 KRW가 아닙니다. transactionId={}, recordId={}, currency={}",
                        transactionId, recordId, currency);

                return;
            }
            if (recordId.isBlank()) {
                transactionIdMap.put(transactionId, "FAILED");

                log.warn("[ERP_ASYNC] recordId가 없습니다. transactionId={}, item={}",
                        transactionId, item);

                return;
            }
            if (!duplicateIDset.add(recordId)) {
                transactionIdMap.put(transactionId, "FAILED");

                log.warn("[ERP_ASYNC] recordId 중복값이 있습니다. transactionId={}, duplicateRecordId={}",
                        transactionId, recordId);

                return;
            }

        }
        erpAsyncComponent.ErpAsyncInsert(payload, transactionIdMap);

    }
}
