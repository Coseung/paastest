package com.example.testapi.domain.clothesorder.component.order;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.dto.order.requestDto.OrderRequestDto;

public interface OrderStrategy {
    boolean isSupported(ItemType itemType);
    void processOrder(OrderRequestDto request);

}
