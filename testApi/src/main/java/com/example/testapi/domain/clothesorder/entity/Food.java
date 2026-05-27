package com.example.testapi.domain.clothesorder.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int stock;

    private String foodName;


    public void decreaseStock(int quantity){
        if(this.stock < quantity){
            throw new IllegalArgumentException("해당 음식 재고가 부족합니다.");
        }
        this.stock -= quantity;
    }
}
