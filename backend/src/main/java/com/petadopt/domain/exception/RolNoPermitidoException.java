package com.petadopt.domain.exception;

import com.petadopt.domain.model.Rol;

public class RolNoPermitidoException extends RuntimeException {
    public RolNoPermitidoException(Rol requerido) {
        super("Esta acción requiere el rol " + requerido);
    }
}