package com.example.bff_operaciones.controller;

import com.example.bff_operaciones.dto.ResponseBodyDto;
import com.example.bff_operaciones.dto.TradeDto;
import com.example.bff_operaciones.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BffController {

    private final TradeService tradeService;

    @GetMapping("/trades")
    public ResponseEntity<ResponseBodyDto> obtenerTrades(){

        List<TradeDto> tradeDtoList = tradeService.obtenerTrades();

        return new ResponseEntity<>(ResponseBodyDto.builder()
                .codigo(200)
                .mensaje("Operaciones obtenidas exitosamente")
                .fechaCreacion(LocalDateTime.now())
                .content(tradeDtoList)
                .fechaError(null)
                .descripcionError(null)
                .build(), HttpStatus.OK);
    }
}
