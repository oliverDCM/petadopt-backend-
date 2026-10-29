package com.petadopt.domain.exception;

public class LimiteSolicitudesExcedidoException extends RuntimeException {
    public LimiteSolicitudesExcedidoException() {
        super("Demasiadas solicitudes de recuperación. Intenta de nuevo en una hora");
    }
}
