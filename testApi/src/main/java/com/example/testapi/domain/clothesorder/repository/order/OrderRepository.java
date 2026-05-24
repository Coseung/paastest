package com.example.testapi.domain.clothesorder.repository.order;

import com.example.testapi.domain.clothesorder.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders,Long> {
}
