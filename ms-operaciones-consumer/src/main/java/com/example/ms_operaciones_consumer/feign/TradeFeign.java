package com.example.ms_operaciones_consumer.feign;

import com.example.ms_operaciones_consumer.dto.TradeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "tradeFeign",
        url = "${azure.functions.trades}"
)
public interface TradeFeign {

    @PostMapping("/api/trades")
    void crearTrade(TradeDto tradeDto);
}
