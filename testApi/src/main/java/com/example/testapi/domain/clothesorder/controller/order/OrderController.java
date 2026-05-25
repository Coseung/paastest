package com.example.testapi.domain.clothesorder.controller.order;

import com.example.testapi.domain.clothesorder.Enum.ItemType;
import com.example.testapi.domain.clothesorder.dto.order.requestDto.OrderRequestDto;
import com.example.testapi.domain.clothesorder.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    @PostMapping("/order")
    public ResponseEntity<String> productOrder(@Valid @RequestBody OrderRequestDto request){

        ItemType itemType =request.getItems().getItemTypeEnum();
        System.out.println(request.getItems().getId());
        orderService.handleOrder(itemType,request);
        return ResponseEntity.ok("주문 요청 완료");
    }
}
