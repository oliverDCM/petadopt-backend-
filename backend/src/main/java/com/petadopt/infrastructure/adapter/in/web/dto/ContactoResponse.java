package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.Contacto;

public record ContactoResponse(String nombre, String ciudad, String email, String telefono) {

    public static ContactoResponse desde(Contacto c) {
        return new ContactoResponse(c.nombre(), c.ciudad(), c.email(), c.telefono());
    }
}
