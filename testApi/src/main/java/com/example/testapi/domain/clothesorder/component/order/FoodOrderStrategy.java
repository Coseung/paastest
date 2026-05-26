package com.example.testapi.domain.clothesorder.component.order;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.Enum.VenderType;
import com.example.testapi.domain.clothesorder.dto.order.requestDto.OrderRequestDto;
import com.example.testapi.domain.clothesorder.entity.Food;
import com.example.testapi.domain.clothesorder.entity.Orders;
import com.example.testapi.domain.clothesorder.repository.order.FoodOrderRepository;
import com.example.testapi.domain.clothesorder.repository.order.OrderRepository;
import com.example.testapi.domain.clothesorder.service.reorder.RestockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FoodOrderStrategy implements OrderStrategy{

    private final FoodOrderRepository foodOrderRepository;
    private final OrderRepository orderRepository;
    private final RestockService restockService;

    @Override
    public boolean isSupported(ItemType itemType) {
        return itemType == ItemType.FOOD;
    }

    @Override
    public void processOrder(OrderRequestDto request) {
        Long itemId = request.getItems().getId();

        Food product = foodOrderRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("[음식 주문 실패] 존재하지 않는 상품 ID: {}", itemId);
                    return new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + itemId);
                });

        log.info("[음식 재고 확인] 상품명: {}, 현재 재고: {}", product.getFoodName(), product.getStock());

        if (product.getStock() < 1) {
            log.warn("[음식 주문 실패] 재고 부족 - 상품 ID: {}, 상품명: {}", itemId, product.getFoodName());
            throw new IllegalStateException("선택하신 상품의 재고가 모두 소진되었습니다.");
        }

        product.decreaseStock(1);
        log.info("[음식 재고 차감 완료] 상품 ID: {}, 남은 재고: {}", itemId, product.getStock());

        if (product.getStock() <10){
            log.info("[의류 재고 10 개 이하 재입고 요청] 해당 이름: {}",product.getFoodName());
            restockService.RestockTrigger(VenderType.AMADON, product.getFoodName(), 100);
            log.info("[의류 재고 재입고 요청 완료]");
        }
        Orders orders =request.ordertoEntity();

        Orders savedOrder = orderRepository.save(orders);
        log.info("[의류 주문 저장 완료]  생성된 주문 Id (pk): {}",savedOrder.getId());

    }
}
