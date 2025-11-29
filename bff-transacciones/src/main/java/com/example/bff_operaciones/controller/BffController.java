package com.example.bff_operaciones.controller;


import com.example.bff_operaciones.dto.CarritoDto;
import com.example.bff_operaciones.dto.CarritoResponse;
import com.example.bff_operaciones.service.CarritoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BffController {

    private final CarritoService carritoService;

    @PostMapping("/carrito")
    public ResponseEntity<CarritoResponse> crearCarrito(@RequestBody CarritoDto carritoDto) {
        log.info("BFF recibió solicitud de creación de carrito: {}", carritoDto);
        CarritoResponse response = carritoService.crearCarrito(carritoDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/carrito/{idCarrito}")
    public ResponseEntity<CarritoResponse> obtenerCarrito(@PathVariable Long idCarrito) {
        log.info("BFF recibió solicitud de obtención de carrito con id: {}", idCarrito);
        CarritoResponse response = carritoService.obtenerCarrito(idCarrito);
        return ResponseEntity.ok(response);
    }

}