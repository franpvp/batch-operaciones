package com.example.bff_operaciones.repository;

import com.example.bff_operaciones.dto.TradeFilter;
import com.example.bff_operaciones.entity.TradeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradeRepositoryCustom {
    Page<TradeEntity> searchByFilter(TradeFilter filter, Pageable pageable);
}
