package com.example.bff_operaciones.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeDto {

    @JsonProperty("ID_TRADE")
    private Long idTrade;
    @JsonProperty("MONTO")
    private Integer monto;
    @JsonProperty("FECHA_CREACION")
    private LocalDate fechaCreacion;
    @JsonProperty("ID_CLIENTE")
    private Long idCliente;
}
