package com.example.paastest.domain.component.test2;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ErpAsyncComponent {

    @Async
    public void ErpAsyncInsert(JsonNode payload, ConcurrentHashMap<String,String> transactionIdMap){
            String transactionId = payload.path("transactionId").asText();
            try {
                //ERP 전송 로직
                transactionIdMap.put(transactionId,"SUCCESS");
            } catch (Exception e) {
                transactionIdMap.put(transactionId,"FAILD");
                throw new RuntimeException(e);
            }
        }


    }

