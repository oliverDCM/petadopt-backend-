package com.petadopt.domain.exception;

public class PerfilNoAccesibleException extends RuntimeException {
    public PerfilNoAccesibleException() {
        super("Solo puedes ver el perfil de quienes enviaron una solicitud sobre tus animales");
    }
}