package com.example.testapi.domain.clothesorder.component.order;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.dto.order.requestdto.OrderRequestDto;
import com.example.testapi.domain.clothesorder.dto.order.responsedto.OrderResponseDto;

public interface OrderStrategy {
    boolean isSupported(ItemType itemType);
    OrderResponseDto processOrder(OrderRequestDto request);

}
