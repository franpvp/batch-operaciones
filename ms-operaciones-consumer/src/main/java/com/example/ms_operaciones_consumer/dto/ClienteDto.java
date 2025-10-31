package com.example.ms_operaciones_consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClienteDto {

    private Long idCliente;
    private String nombre;
    private String apellido;
    private String correo;
    private Integer edad;

}
