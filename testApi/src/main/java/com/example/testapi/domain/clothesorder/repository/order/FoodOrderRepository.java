package com.example.testapi.domain.clothesorder.repository.order;


import com.example.testapi.domain.clothesorder.entity.Food;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FoodOrderRepository extends JpaRepository<Food,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Food f where f.id = :id")
    Optional<Food> findByIdForUpdate(@Param("id") Long id);

}
