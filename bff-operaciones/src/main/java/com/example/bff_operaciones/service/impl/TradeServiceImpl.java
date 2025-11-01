package com.example.bff_operaciones.service.impl;

import com.example.bff_operaciones.dto.TradeDto;
import com.example.bff_operaciones.dto.TradeFilter;
import com.example.bff_operaciones.entity.TradeEntity;
import com.example.bff_operaciones.mapper.TradesMapper;
import com.example.bff_operaciones.repository.TradeRepository;
import com.example.bff_operaciones.service.TradeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;

    @Override
    public List<TradeDto> obtenerTrades() {
        List<TradeEntity> tradeEntities = tradeRepository.findAll();
        return tradeEntities.stream().map(TradesMapper::entityToDto).toList();
    }

    @Override
    @Transactional
    public Page<TradeDto> search(TradeFilter filter, Pageable pageable) {
        Page<TradeEntity> page = tradeRepository.searchByFilter(filter, pageable);
        return page.map(TradesMapper::entityToDto);
    }

}