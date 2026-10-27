package com.petadopt.domain.exception;

public class SoloAdoptantePuedeSolicitarException extends RuntimeException {
    public SoloAdoptantePuedeSolicitarException() {
        super("Solo los usuarios con rol ADOPTANTE pueden enviar solicitudes");
    }
}