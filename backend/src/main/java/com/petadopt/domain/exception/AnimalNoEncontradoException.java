package com.petadopt.domain.exception;

public class AnimalNoEncontradoException extends RuntimeException {
    public AnimalNoEncontradoException(Long id) {
        super("No existe un animal con id " + id);
    }
}