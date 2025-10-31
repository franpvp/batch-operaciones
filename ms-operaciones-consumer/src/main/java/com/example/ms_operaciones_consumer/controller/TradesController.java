package com.example.ms_operaciones_consumer.controller;

import com.example.ms_operaciones_consumer.dto.TradeDto;
import com.example.ms_operaciones_consumer.feign.TradeFeign;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trades")
public class TradesController {

    private final TradeFeign tradeFeign;

    public TradesController(TradeFeign tradeFeign) {
        this.tradeFeign = tradeFeign;
    }

    @PostMapping
    public void crearProducto(@RequestBody TradeDto tradeDto) {
        tradeFeign.crearTrade(tradeDto);
    }

}
