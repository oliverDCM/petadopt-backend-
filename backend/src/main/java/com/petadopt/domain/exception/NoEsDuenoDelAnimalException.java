package com.petadopt.domain.exception;

public class NoEsDuenoDelAnimalException extends RuntimeException {
    public NoEsDuenoDelAnimalException() {
        super("Solo el vendedor dueño del animal puede decidir sobre sus solicitudes");
    }

    public NoEsDuenoDelAnimalException(String mensaje) {
        super(mensaje);
    }
}
