package com.petadopt.domain.exception;

public class SolicitudNoEncontradaException extends RuntimeException {
    public SolicitudNoEncontradaException(Long id) {
        super("No existe una solicitud con id " + id);
    }
}