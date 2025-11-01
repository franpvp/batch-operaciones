package com.example.bff_operaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TradeFilter {

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String canal;
    private Long idTrade;
}
