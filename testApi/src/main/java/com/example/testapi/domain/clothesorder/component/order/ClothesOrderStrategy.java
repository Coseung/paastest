package com.example.testapi.domain.clothesorder.component.order;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.Enum.VenderType;
import com.example.testapi.domain.clothesorder.dto.order.requestdto.OrderRequestDto;
import com.example.testapi.domain.clothesorder.dto.order.responsedto.OrderResponseDto;
import com.example.testapi.domain.clothesorder.entity.Clothes;
import com.example.testapi.domain.clothesorder.entity.Orders;
import com.example.testapi.domain.clothesorder.repository.order.ClothesOrderRepository;
import com.example.testapi.domain.clothesorder.repository.order.OrderRepository;
import com.example.testapi.domain.clothesorder.service.reorder.RestockService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClothesOrderStrategy implements OrderStrategy{

    private final ClothesOrderRepository clothesOrderRepository;
    private final OrderRepository orderRepository;
    private final RestockService restockService;
    private final LocalValidatorFactoryBean localValidatorFactoryBean;

    @Override
    public boolean isSupported(ItemType itemType) {
        return itemType == ItemType.CLOTHES;
    }

    @Override
    @Transactional
    public OrderResponseDto processOrder(OrderRequestDto request) {

        Long itemId = request.getItems().getId();

        Clothes product = clothesOrderRepository.findbyIdForUpdate(itemId)
                .orElseThrow(() -> {
                    log.warn("[의류 주문 실패] 존재하지 않는 상품 ID: {}", itemId);
                    return new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + itemId);
                });
        log.info("[의류 재고 확인] 상품명: {}, 현재 재고: {}", product.getClothesName(), product.getStock());

        if (product.getStock() < 1) {
            log.warn("[의류 주문 실패] 재고 부족 - 상품 ID: {}, 상품명: {}", itemId, product.getClothesName());
            throw new IllegalStateException("선택하신 상품의 재고가 모두 소진되었습니다.");
        }

        product.decreaseStock(1);
        log.info("[의류 재고 차감 완료] 상품 ID: {}, 남은 재고: {}", itemId, product.getStock());

        if (product.getStock() <10){
            log.info("[의류 재고 10 개 이하 재입고 요청] 해당 이름: {}",product.getClothesName());
            restockService.RestockTrigger(VenderType.COUMANG, product.getClothesName(), 100);
            log.info("[의류 재고 재입고 요청 완료]");
        }
        Orders orders =request.ordertoEntity();

        Orders savedOrder = orderRepository.save(orders);
        log.info("[의류 주문 저장 완료]  생성된 주문 Id (pk): {}",savedOrder.getId());
        return OrderResponseDto.builder()
                .id(savedOrder.getId())
                .email(savedOrder.getContactEmail())
                .itemName(product.getClothesName())
                .build();
    }
}
