package com.example.bff_operaciones.controller.advice;

import com.example.bff_operaciones.dto.ErrorResponse;
import com.example.bff_operaciones.exception.ServicioTransaccionesNoDisponibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServicioTransaccionesNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleTransaccionesDown(ServicioTransaccionesNoDisponibleException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .codigo("TRANSACCIONES_NO_DISPONIBLE")
                .mensaje(ex.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(503).body(error);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestBody(HttpMessageNotReadableException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .codigo("REQUEST_BODY_INVALIDO")
                .mensaje("El cuerpo de la petición es obligatorio o está mal formado")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
