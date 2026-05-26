package com.example.testapi.domain.clothesorder.service.reorder;

import com.example.testapi.domain.clothesorder.Enum.VenderType;

public interface RestockService {

    void RestockTrigger(VenderType venderType, String itemName, int stock);
}
