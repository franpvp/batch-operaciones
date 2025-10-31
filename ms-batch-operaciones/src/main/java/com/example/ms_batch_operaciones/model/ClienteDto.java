
package com.example.ms_batch_operaciones.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClienteDto {
  private String idCliente;
  private String nombre;
  private String apellido;
  private String correo;
  private Integer edad;
}
