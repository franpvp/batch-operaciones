package com.example.bff_operaciones.exception;



public class ServicioTransaccionesNoDisponibleException extends RuntimeException {

    public ServicioTransaccionesNoDisponibleException(String message) {
        super(message);
    }

    public ServicioTransaccionesNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
