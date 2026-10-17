package com.petadopt.domain.exception;

public class VendedorNoAutorizadoException extends RuntimeException {
    public VendedorNoAutorizadoException() {
        super("Solo los usuarios con rol VENDEDOR pueden publicar animales");
    }
}