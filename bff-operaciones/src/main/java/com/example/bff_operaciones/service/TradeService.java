package com.example.bff_operaciones.service;

import com.example.bff_operaciones.dto.TradeDto;
import com.example.bff_operaciones.dto.TradeFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TradeService {

    List<TradeDto> obtenerTrades();
    Page<TradeDto> search(TradeFilter filter, Pageable pageable);
}
