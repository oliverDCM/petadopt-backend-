package com.petadopt.domain.exception;

public class ContactoNoDisponibleException extends RuntimeException {
    public ContactoNoDisponibleException() {
        super("Los datos de contacto solo están disponibles para las partes de una solicitud aprobada");
    }
}
