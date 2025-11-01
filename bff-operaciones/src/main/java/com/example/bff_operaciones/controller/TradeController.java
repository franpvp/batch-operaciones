package com.example.bff_operaciones.controller;

import com.example.bff_operaciones.dto.TradeDto;
import com.example.bff_operaciones.dto.TradeFilter;
import com.example.bff_operaciones.service.TradeService;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping
    public Page<TradeDto> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String canal,
            @RequestParam(required = false) Long idTrade,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaCreacion,desc") String sort
    ) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        TradeFilter filter = new TradeFilter();
        filter.setFechaDesde(fechaDesde);
        filter.setFechaHasta(fechaHasta);
        filter.setCanal(canal);
        filter.setIdTrade(idTrade);

        return tradeService.search(filter, pageable);
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "fechaCreacion");
        }
        String[] parts = sortParam.split(",");
        String prop = parts[0];
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, prop);
    }
}
