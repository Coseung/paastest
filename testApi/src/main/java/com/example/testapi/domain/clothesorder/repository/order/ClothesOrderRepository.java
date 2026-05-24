package com.example.testapi.domain.clothesorder.repository.order;


import com.example.testapi.domain.clothesorder.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesOrderRepository extends JpaRepository<Clothes,Long> {

}
