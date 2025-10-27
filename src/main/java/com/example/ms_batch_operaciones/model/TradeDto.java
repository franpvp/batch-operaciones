package com.example.ms_batch_operaciones.model;

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
public class TradeDto {

    private Long idTrade;
    private Long monto;
    private LocalDate fechaCreacion;
    private Long idCliente;
}
