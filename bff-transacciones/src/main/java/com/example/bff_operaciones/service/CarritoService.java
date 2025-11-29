package com.example.bff_operaciones.service;

import com.example.bff_operaciones.dto.CarritoDto;
import com.example.bff_operaciones.dto.CarritoResponse;
import com.example.bff_operaciones.exception.ServicioTransaccionesNoDisponibleException;
import com.example.bff_operaciones.feign.OrdenClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarritoService {

    private final OrdenClient ordenClient;

    @CircuitBreaker(name = "transaccionesService", fallbackMethod = "crearCarritoFallback")
    public CarritoResponse crearCarrito(CarritoDto carritoDto) {
        log.info("Creando carrito para request: {}", carritoDto);
        return ordenClient.crearCarrito(carritoDto);
    }

    @CircuitBreaker(name = "transaccionesService", fallbackMethod = "obtenerCarritoFallback")
    public CarritoResponse obtenerCarrito(Long idCarrito) {
        log.info("Llamando a transacciones-service para obtener carrito id={}", idCarrito);
        return ordenClient.obtenerCarrito(idCarrito);
    }

    private CarritoResponse crearCarritoFallback(CarritoDto carritoDto, Throwable ex) {
        if (ex instanceof CallNotPermittedException) {
            log.warn("Circuit breaker transaccionesService OPEN. Fallback rápido para request: {}", carritoDto);
        } else {
            log.error("Error llamando a transaccionesService. Request: {}, error: {}", carritoDto, ex.getMessage(), ex);
        }
        throw new ServicioTransaccionesNoDisponibleException(
                "Servicio de transacciones no disponible, intente más tarde", ex
        );
    }

    private CarritoResponse obtenerCarritoFallback(Long idCarrito,Throwable ex) {
        if (ex instanceof CallNotPermittedException) {
            log.warn("Circuit breaker obtenerCarrito OPEN. Fallback rápido para id carrito: {}", idCarrito);
        } else {
            log.error("Fallback obtenerCarrito id={} por error en transacciones-service: {}", idCarrito, ex.getMessage(), ex);
        }
        throw new ServicioTransaccionesNoDisponibleException(
                "Servicio de transacciones no disponible, intente más tarde", ex
        );
    }
}
