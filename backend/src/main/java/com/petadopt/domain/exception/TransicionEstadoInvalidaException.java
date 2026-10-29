package com.petadopt.domain.exception;

import com.petadopt.domain.model.EstadoAnimal;

public class TransicionEstadoInvalidaException extends RuntimeException {
    public TransicionEstadoInvalidaException(EstadoAnimal desde, EstadoAnimal hacia) {
        super("No se puede pasar un animal de " + desde + " a " + hacia);
    }
}
