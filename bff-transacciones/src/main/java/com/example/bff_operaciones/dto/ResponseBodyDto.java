package com.example.bff_operaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResponseBodyDto {

    private Integer codigo;
    private String mensaje;
    private LocalDateTime fechaCreacion;
    private Object content;
    private String fechaError;
    private String descripcionError;

}
