package com.example.testapi.domain.clothesorder.component;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.dto.order.requestDto.OrderRequestDto;
import com.example.testapi.domain.clothesorder.entity.Clothes;
import com.example.testapi.domain.clothesorder.entity.Orders;
import com.example.testapi.domain.clothesorder.repository.order.ClothesOrderRepository;
import com.example.testapi.domain.clothesorder.repository.order.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.beans.Transient;

@Component
@RequiredArgsConstructor
public class ClothesOrderStrategy implements OrderStrategy{

    private final ClothesOrderRepository clothesOrderRepository;
    private final OrderRepository orderRepository;
    @Override
    public boolean isSupported(ItemType itemType) {
        return itemType == ItemType.CLOTHES;
    }

    @Override
    @Transactional
    public void processOrder(OrderRequestDto request) {

        Long itemId = request.getItems().getItemId();

        Clothes product = clothesOrderRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + itemId));

        if (product.getStock() < 1) {
            throw new IllegalStateException("선택하신 상품의 재고가 모두 소진되었습니다.");
        }

        product.decreaseStock(1);
        Orders orders =request.ordertoEntity();

        orderRepository.save(orders);
    }
}
