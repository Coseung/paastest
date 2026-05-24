package com.example.testapi.domain.clothesorder.service.order;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.dto.order.requestDto.OrderRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    public void handleOrder(ItemType itemType, OrderRequestDto request);
}
