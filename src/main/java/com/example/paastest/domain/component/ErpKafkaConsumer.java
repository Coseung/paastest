package com.example.paastest.domain.component;

import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ErpKafkaConsumer {

    // 초당 50건 처리 제한
    private final RateLimiter rateLimiter = RateLimiter.create(50.0);

    @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @KafkaListener(
            topics = "erp-order-topic",
            groupId = "erp-order-group",
            concurrency = "1"
    )
    public void consume(String message) {
        try {
            rateLimiter.acquire();

            // ERP 전송 로직
            // erpService.sendToErp(message);

            log.info("[ERP_CONSUMER] 메시지 처리 성공. message={}", message);

        } catch (Exception e) {
            log.error("[ERP_CONSUMER] 메시지 처리 실패. 재시도 대상입니다. message={}", message, e);

            // 중요:
            // 여기서 예외를 다시 던져야 @RetryableTopic이 재시도합니다.
            throw e;
        }
    }

    @DltHandler
    public void dltHandler(String message) {
        log.error("[ERP_CONSUMER_DLT] 모든 재시도 실패 후 DLT로 이동한 메시지입니다. message={}", message);

        // 여기서 최종 실패 처리
        // 예: DB에 실패 이력 저장, Slack 알림, 운영자 알림 등
    }
}