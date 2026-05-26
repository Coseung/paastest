package com.example.testapi.domain.clothesorder.component.reorder;

import com.example.testapi.domain.clothesorder.Enum.VenderType;

public interface RestockStrategy {
    boolean isSuppored(VenderType venderType);
    void requestRestock(String itemName, int stock);
}
