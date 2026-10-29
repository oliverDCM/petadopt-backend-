package com.petadopt.domain.model;

/** Datos de contacto de la otra parte de una solicitud aprobada. */
public record Contacto(String nombre, String ciudad, String email, String telefono) {

    public static Contacto de(Usuario usuario) {
        return new Contacto(usuario.nombre(), usuario.ciudad(), usuario.email(), usuario.telefono());
    }
}
