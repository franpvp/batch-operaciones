package com.example.bff_operaciones.repository;

import com.example.bff_operaciones.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository
        extends JpaRepository<TradeEntity, Long>, TradeRepositoryCustom {
}
