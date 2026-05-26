package com.example.testapi.domain.clothesorder.repository.reorder;

import com.example.testapi.domain.clothesorder.entity.Restock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestockRepository extends JpaRepository<Restock,Long> {

}
