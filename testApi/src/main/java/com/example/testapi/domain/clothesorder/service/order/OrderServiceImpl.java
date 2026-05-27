package com.example.testapi.domain.clothesorder.service.order;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.component.order.OrderStrategy;
import com.example.testapi.domain.clothesorder.dto.order.requestdto.OrderRequestDto;
import com.example.testapi.domain.clothesorder.dto.order.responsedto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final List<OrderStrategy> orderStrategies;

    @Override
    public OrderResponseDto handleOrder(ItemType itemType, OrderRequestDto request) {
        log.info("[주문 시작] 이메일: {}, 주문 타입: {}",
                request.getContactInfo().getContactEmail(), itemType);
        OrderStrategy targetStrategy = orderStrategies.stream()
                .filter(strategy -> strategy.isSupported(itemType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 주문 타입입니다: " + itemType));

        try {
            OrderResponseDto responseDto = targetStrategy.processOrder(request);
            log.info("[주문 성공] 이메일: {}, 주문 타입: {} 처리 완료",
                    request.getContactInfo().getContactEmail(), itemType);
            return responseDto;

        } catch (Exception e) {
            log.error("[주문 오류 발생] 이메일: {}, 사유: {}",
                    request.getContactInfo().getContactEmail(), e.getMessage(), e);
            throw new RuntimeException("주문 진행중 오류가 발생하였습니다 : {}",e);
        }


    }
}
