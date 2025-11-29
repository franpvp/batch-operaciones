package com.example.bff_operaciones.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CarritoItemResponse {
    private Long idProducto;
    private int cantidad;
}
