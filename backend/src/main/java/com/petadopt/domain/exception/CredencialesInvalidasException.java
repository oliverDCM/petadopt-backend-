package com.petadopt.domain.exception;

/**
 * Se lanza cuando el email o la contraseña no coinciden. El mensaje es genérico
 * a propósito: no revela si el email existe o no.
 */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Email o contraseña incorrectos");
    }
}
