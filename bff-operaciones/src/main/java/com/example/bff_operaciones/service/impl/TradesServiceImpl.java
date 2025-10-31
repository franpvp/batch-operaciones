package com.example.bff_operaciones.service.impl;

import com.example.bff_operaciones.dto.TradeDto;
import com.example.bff_operaciones.entity.TradeEntity;
import com.example.bff_operaciones.mapper.TradesMapper;
import com.example.bff_operaciones.repository.TradesRepository;
import com.example.bff_operaciones.service.TradesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradesServiceImpl implements TradesService {

    private final TradesRepository tradesRepository;

    @Override
    public List<TradeDto> obtenerMensajesOperaciones() {

        List<TradeEntity> tradeEntities = tradesRepository.findAll();

        return tradeEntities.stream()
                .map(TradesMapper::entityToDto)
                .toList();
    }
}
