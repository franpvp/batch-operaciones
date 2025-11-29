package com.example.bff_operaciones.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CarritoResponse {
    private Long idCarrito;
    private List<CarritoItemResponse> items;
}
