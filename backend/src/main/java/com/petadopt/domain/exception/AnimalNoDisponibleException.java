package com.petadopt.domain.exception;

public class AnimalNoDisponibleException extends RuntimeException {
    public AnimalNoDisponibleException() {
        super("El animal ya no está disponible");
    }
}