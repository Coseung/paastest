package com.example.testapi.domain.clothesorder.service.order;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.component.OrderStrategy;
import com.example.testapi.domain.clothesorder.dto.order.requestDto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final List<OrderStrategy> orderStrategies;

    @Override
    public void handleOrder(ItemType itemType, OrderRequestDto request) {
        OrderStrategy targetStrategy = orderStrategies.stream()
                .filter(strategy -> strategy.isSupported(itemType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 주문 타입입니다: " + itemType));

        try {
            targetStrategy.processOrder(request);

        } catch (Exception e) {
            throw new RuntimeException("주문 진행중 오류가 발생하였습니다 : {}",e);
        }


    }
}
