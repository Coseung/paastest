package com.example.paastest.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErpKafkaService {
    private static final String NODE_NAME = "ERP_ORDER_SERVICE";
    private static final String TOPIC_NAME = "erp-order-topic";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final String PROCESSING = "PROCESSING";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, String> webhookIdMap = new ConcurrentHashMap<>();

    public void sendOrderToErp(JsonNode payload) {
        String webhookId = payload.path("webhook_id").asText();

        if (webhookId.isBlank()) {
            log.warn("[{}] webhook_id가 없습니다. payload={}", NODE_NAME, payload);
            return;
        }

        /*
         * 중복 요청 방어
         *
         * webhookId가 없으면 PROCESSING 저장 후 null 반환
         * webhookId가 이미 있으면 기존 상태 반환
         */
        String previousState = webhookIdMap.putIfAbsent(webhookId, PROCESSING);

        if (previousState != null) {
            log.warn("[{}] 중복 webhook 요청입니다. webhookId={}, previousState={}",
                    NODE_NAME, webhookId, previousState);
            return;
        }

        JsonNode data = payload.path("data");

        String orderId = data.path("order_id").asText();
        String customerId = data.path("customer_id").asText();
        JsonNode items = data.path("items");

        if (orderId.isBlank()) {
            webhookIdMap.put(webhookId, FAILED);
            log.warn("[{}] order_id가 없습니다. webhookId={}, payload={}",
                    NODE_NAME, webhookId, payload);
            return;
        }

        if (customerId.isBlank()) {
            webhookIdMap.put(webhookId, FAILED);
            log.warn("[{}] customer_id가 없습니다. webhookId={}, orderId={}",
                    NODE_NAME, webhookId, orderId);
            return;
        }

        if (!items.isArray() || items.isEmpty()) {
            webhookIdMap.put(webhookId, FAILED);
            log.warn("[{}] items가 배열이 아니거나 비어 있습니다. webhookId={}, orderId={}",
                    NODE_NAME, webhookId, orderId);
            return;
        }

        Set<String> itemCodeSet = new HashSet<>();
        BigDecimal totalOrderAmount = BigDecimal.ZERO;

        for (JsonNode item : items) {
            String itemCode = item.path("item_code").asText();
            JsonNode qtyNode = item.path("qty");
            JsonNode priceNode = item.path("price");

            if (itemCode.isBlank()) {
                webhookIdMap.put(webhookId, FAILED);
                log.warn("[{}] item_code가 없습니다. webhookId={}, orderId={}, item={}",
                        NODE_NAME, webhookId, orderId, item);
                return;
            }

            if (!itemCodeSet.add(itemCode)) {
                webhookIdMap.put(webhookId, FAILED);
                log.warn("[{}] item_code 중복값이 있습니다. webhookId={}, orderId={}, duplicateItemCode={}",
                        NODE_NAME, webhookId, orderId, itemCode);
                return;
            }

            if (!qtyNode.isNumber()) {
                webhookIdMap.put(webhookId, FAILED);
                log.warn("[{}] qty가 숫자가 아닙니다. webhookId={}, orderId={}, itemCode={}, qty={}",
                        NODE_NAME, webhookId, orderId, itemCode, qtyNode);
                return;
            }

            int qty = qtyNode.asInt();

            if (qty < 0) {
                webhookIdMap.put(webhookId, FAILED);
                log.warn("[{}] qty가 음수입니다. webhookId={}, orderId={}, itemCode={}, qty={}",
                        NODE_NAME, webhookId, orderId, itemCode, qty);
                return;
            }

            if (!priceNode.isNumber()) {
                webhookIdMap.put(webhookId, FAILED);
                log.warn("[{}] price가 숫자가 아닙니다. webhookId={}, orderId={}, itemCode={}, price={}",
                        NODE_NAME, webhookId, orderId, itemCode, priceNode);
                return;
            }

            BigDecimal price = priceNode.decimalValue();

            if (price.compareTo(BigDecimal.ZERO) < 0) {
                webhookIdMap.put(webhookId, FAILED);
                log.warn("[{}] price가 음수입니다. webhookId={}, orderId={}, itemCode={}, price={}",
                        NODE_NAME, webhookId, orderId, itemCode, price);
                return;
            }

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty));
            totalOrderAmount = totalOrderAmount.add(itemTotal);
        }

        ObjectNode erpPayload = objectMapper.createObjectNode();

        erpPayload.put("erp_order_no", orderId);
        erpPayload.put("buyer_id", customerId);
        erpPayload.put("total_order_amount", totalOrderAmount);

        String receivedAt = OffsetDateTime.now(KST)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        erpPayload.put("received_at", receivedAt);

        log.info("[{}] ERP 전송용 payload 변환 완료. webhookId={}, payload={}",
                NODE_NAME, webhookId, erpPayload);

        /*
         * Kafka 전송 위치
         *
         * 실제 Kafka 연동 시에는 아래와 같은 방식으로 전송합니다.
         *
         * kafkaTemplate.send(TOPIC_NAME, orderId, erpPayload.toString())
         *         .whenComplete((result, ex) -> {
         *             if (ex != null) {
         *                 webhookIdMap.put(webhookId, FAILED);
         *
         *                 log.error("[{}] Kafka 전송 실패. webhookId={}, orderId={}, payload={}",
         *                         NODE_NAME, webhookId, orderId, erpPayload, ex);
         *                 return;
         *             }
         *
         *             webhookIdMap.put(webhookId, SUCCESS);
         *
         *             log.info("[{}] Kafka 전송 성공. topic={}, webhookId={}, orderId={}",
         *                     NODE_NAME, TOPIC_NAME, webhookId, orderId);
         *         });
         */

        /*
         * 현재는 Kafka 전송이 주석 처리되어 있으므로,
         * 변환까지 성공하면 SUCCESS로 처리합니다.
         */
        webhookIdMap.put(webhookId, SUCCESS);

    }
}
