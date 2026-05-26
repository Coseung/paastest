package com.example.testapi.domain.clothesorder.service.reorder;


import com.example.testapi.domain.clothesorder.Enum.VenderType;
import com.example.testapi.domain.clothesorder.component.reorder.RestockStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestockServiceImpl implements RestockService {

    private final List<RestockStrategy> restockStrategies;
    @Override
    public void RestockTrigger(VenderType venderType, String itemName, int stock) {

        RestockStrategy targetStrategy = restockStrategies.stream()
                .filter(strategy -> strategy.isSuppored(venderType))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("지원하지 않은 벤더사 입니다."));

        targetStrategy.requestRestock(itemName, stock);
    }
}
