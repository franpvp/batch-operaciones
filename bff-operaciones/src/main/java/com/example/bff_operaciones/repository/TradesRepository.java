package com.example.bff_operaciones.repository;

import com.example.bff_operaciones.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradesRepository extends JpaRepository<TradeEntity, Long> {
}
