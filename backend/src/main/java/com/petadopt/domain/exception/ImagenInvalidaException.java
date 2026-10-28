package com.petadopt.domain.exception;

public class ImagenInvalidaException extends RuntimeException {
    public ImagenInvalidaException(String mensaje) {
        super(mensaje);
    }
}