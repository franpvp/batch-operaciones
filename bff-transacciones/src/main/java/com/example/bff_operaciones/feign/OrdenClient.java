package com.example.bff_operaciones.feign;

import com.example.bff_operaciones.dto.CarritoDto;
import com.example.bff_operaciones.dto.CarritoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "transacciones-service",
        url = "${transacciones.service.url}" // ej: http://ms-transacciones:8080
)
public interface OrdenClient {

    @PostMapping("/api/v1/transacciones")
    CarritoResponse crearCarrito(@RequestBody CarritoDto carritoDto);

    @GetMapping("/api/v1/transacciones/{idCarrito}")
    CarritoResponse obtenerCarrito(@PathVariable("idCarrito") Long idCarrito);
}
