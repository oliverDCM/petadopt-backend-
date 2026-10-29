package com.petadopt.domain.exception;

public class ContrasenaInvalidaException extends RuntimeException {
    public ContrasenaInvalidaException() {
        super("La contraseña debe tener entre 8 y 72 caracteres");
    }
}
