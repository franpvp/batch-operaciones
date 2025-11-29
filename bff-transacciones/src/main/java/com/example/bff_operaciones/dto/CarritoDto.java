package com.example.bff_operaciones.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CarritoDto {
    private Long idCliente;
    private Long idProducto;
    private int cantidad;
}
