package com.example.paastest.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // "스프링아, 우리 서버에서 터지는 모든 에러는 내가 여기서 통제할게"
public class GlobalExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleExternalApiError(RestClientResponseException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorCode", "ERR_EXTERNAL_API");
        errorResponse.put("message", "타겟 시스템 연동 중 오류가 발생했습니다.");
        errorResponse.put("targetStatusCode", ex.getStatusCode().value());
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        // 우리 잘못(500)이 아니라, 타겟 서버와 통신 실패임을 뜻하는 502 Bad Gateway로 변환
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    // 2. 외부 API가 5초 동안 응답이 없을 때 (타임아웃)
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleTimeoutError(ResourceAccessException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorCode", "ERR_GATEWAY_TIMEOUT");
        errorResponse.put("message", "타겟 시스템이 응답하지 않습니다.");
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse);
    }

    @ExceptionHandler(NodeExecutionException.class)
    public ResponseEntity<Map<String, Object>> handleNodeException(NodeExecutionException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorCode", "ERR_PIPELINE_STOPPED");
        // 예외 객체에서 우리가 심어둔 노드 이름을 꺼내서 메시지에 씁니다!
        errorResponse.put("message", "파이프라인 중단. 실패한 노드: " + ex.getNodeName());
        errorResponse.put("detail", ex.getMessage());

        // 500이 아닌 502 Bad Gateway로 처리
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }
}
