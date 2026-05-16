package com.example.paastest.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineOrchestratorService {

    // 각 작업자 노드들을 의존성 주입(DI) 받습니다.
    private final WebhookService webhookService;
    private final SlackNodeService slackNodeService;
//    private final EmailNodeService emailNodeService;

    public void executePipeline(JsonNode payload) {
        log.info("========== [파이프라인 시작] ==========");

        // 작업자들에게 순서대로 일을 시킵니다.
        // 내부에서 NodeExecutionException이 터지면 그 즉시 아래 코드는 무시되고 중단됩니다 (Fail-Fast)
        webhookService.processAndSendToErp(payload);
        slackNodeService.process(payload);
//        emailNodeService.process(payload);

        log.info("========== [파이프라인 모든 노드 정상 종료] ==========");
    }
}
