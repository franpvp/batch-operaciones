package com.example.bff_operaciones.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClienteDto {

    @JsonProperty("ID_CLIENTE")
    private Long idCliente;
    @JsonProperty("RUT_CLIENTE")
    private String rutCliente;
    @JsonProperty("NOMBRE")
    private String nombre;
    @JsonProperty("APELLIDO")
    private String apellido;
    @JsonProperty("EDAD")
    private Integer edad;
}
