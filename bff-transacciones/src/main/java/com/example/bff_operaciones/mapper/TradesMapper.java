package com.example.bff_operaciones.mapper;

import com.example.bff_operaciones.dto.TradeDto;
import com.example.bff_operaciones.entity.TradeEntity;

public final class TradesMapper {

    public static TradeDto entityToDto(TradeEntity tradeEntity){
        return TradeDto.builder()
                .idTrade(tradeEntity.getIdTrade())
                .monto(tradeEntity.getMonto())
                .canal(tradeEntity.getCanal())
                .fechaCreacion(tradeEntity.getFechaCreacion())
                .idCliente(tradeEntity.getIdCliente())
                .build();
    }

    public static TradeEntity dtoToEntity(TradeDto tradeDto){
        return TradeEntity.builder()
                .idTrade(tradeDto.getIdTrade())
                .monto(tradeDto.getMonto())
                .canal(tradeDto.getCanal())
                .fechaCreacion(tradeDto.getFechaCreacion())
                .idCliente(tradeDto.getIdCliente())
                .build();
    }
}
