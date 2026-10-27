package com.petadopt.domain.exception;

public class SolicitudYaDecididaException extends RuntimeException {
    public SolicitudYaDecididaException() {
        super("La solicitud ya fue decidida y no puede modificarse");
    }
}