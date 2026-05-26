package com.example.testapi.domain.clothesorder.component.reorder;

import com.example.testapi.domain.clothesorder.Enum.VenderType;
import com.example.testapi.domain.clothesorder.entity.Restock;
import com.example.testapi.domain.clothesorder.repository.reorder.RestockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AmadonStrategy implements RestockStrategy{

    private final RestockRepository restockRepository;
    @Override
    public boolean isSuppored(VenderType venderType) {
        return venderType == VenderType.AMADON;
    }

    @Override
    public void requestRestock(String itemName, int stock) {
        String encrypyName = itemName + "123";

        Restock request = Restock.builder()
                .itemName(itemName)
                .encryptName(encrypyName)
                .stock(stock)
                .build();
        try{
            restockRepository.save(request);
            log.info("[재입고 요청 테이블 저장 완료] 벤더사: AMADON 아이템: {} 재입고 재고: {}",itemName,stock );
        }catch (RuntimeException e){
            log.error("[재입고 요청중 오류가 발생하였습니다.] "+e.getMessage());
        }

    }
}
