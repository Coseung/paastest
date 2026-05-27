package com.example.testapi.domain.clothesorder.service.order;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.dto.order.requestdto.OrderRequestDto;
import com.example.testapi.domain.clothesorder.dto.order.responsedto.OrderResponseDto;


public interface OrderService {
     OrderResponseDto handleOrder(ItemType itemType, OrderRequestDto request);
}
