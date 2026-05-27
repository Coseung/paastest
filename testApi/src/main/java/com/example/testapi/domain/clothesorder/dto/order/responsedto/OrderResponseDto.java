package com.example.testapi.domain.clothesorder.dto.order.responsedto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderResponseDto {
    private Long id;
    private String itemName;
    private String email;
}
