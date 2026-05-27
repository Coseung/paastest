package com.example.testapi.domain.clothesorder.entity;


import jakarta.persistence.*;
import lombok.Getter;

import javax.naming.Name;

@Entity
@Getter
public class Clothes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int stock;

    private String clothesName;

    public void decreaseStock(int quantity){
        if(this.stock < quantity){
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }
}
