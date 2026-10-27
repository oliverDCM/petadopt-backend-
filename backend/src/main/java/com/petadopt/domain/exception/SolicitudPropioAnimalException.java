package com.petadopt.domain.exception;

public class SolicitudPropioAnimalException extends RuntimeException {
    public SolicitudPropioAnimalException() {
        super("No puedes solicitar tu propio animal");
    }
}