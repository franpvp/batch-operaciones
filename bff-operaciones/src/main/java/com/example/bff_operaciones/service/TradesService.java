package com.example.bff_operaciones.service;

import com.example.bff_operaciones.dto.ResponseBodyDto;
import com.example.bff_operaciones.dto.TradeDto;

import java.util.List;

public interface TradesService {

    List<TradeDto> obtenerMensajesOperaciones();
}
