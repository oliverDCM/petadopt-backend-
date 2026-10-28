package com.petadopt.domain.exception;

public class AlmacenamientoImagenException extends RuntimeException {
    public AlmacenamientoImagenException(Throwable causa) {
        super("No se pudo almacenar la imagen en el servicio externo", causa);
    }
}