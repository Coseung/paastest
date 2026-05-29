package com.example.testapi.domain.clothesorder.repository.order;


import com.example.testapi.domain.clothesorder.entity.Clothes;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClothesOrderRepository extends JpaRepository<Clothes,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Clothes c where c.id= :id")
    Optional<Clothes> findbyIdForUpdate(@Param("id") Long id);

}
