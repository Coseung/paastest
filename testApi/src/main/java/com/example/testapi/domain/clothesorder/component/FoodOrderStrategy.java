package com.example.testapi.domain.clothesorder.component;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.dto.order.requestDto.OrderRequestDto;
import com.example.testapi.domain.clothesorder.entity.Clothes;
import com.example.testapi.domain.clothesorder.entity.Food;
import com.example.testapi.domain.clothesorder.entity.Orders;
import com.example.testapi.domain.clothesorder.repository.order.FoodOrderRepository;
import com.example.testapi.domain.clothesorder.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FoodOrderStrategy implements OrderStrategy{

    private final FoodOrderRepository foodOrderRepository;
    private final OrderRepository orderRepository;

    @Override
    public boolean isSupported(ItemType itemType) {
        return itemType == ItemType.FOOD;
    }

    @Override
    public void processOrder(OrderRequestDto request) {
        Long itemId = request.getItems().getItemId();

        Food product = foodOrderRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + itemId));

        if (product.getStock() < 1) {
            throw new IllegalStateException("선택하신 상품의 재고가 모두 소진되었습니다.");
        }

        product.decreaseStock(1);
        Orders orders =request.ordertoEntity();

        orderRepository.save(orders);
    }
}
