package com.example.testapi.domain.clothesorder.repository.order;


import com.example.testapi.domain.clothesorder.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodOrderRepository extends JpaRepository<Food,Long> {
}
