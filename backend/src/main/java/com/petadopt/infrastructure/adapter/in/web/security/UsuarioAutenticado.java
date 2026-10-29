package com.petadopt.infrastructure.adapter.in.web.security;

import com.petadopt.domain.model.Rol;

import java.security.Principal;

/**
 * Principal que queda en el SecurityContext tras validar el token.
 * Implementa Principal para que Authentication.getName() devuelva el email.
 */
public record UsuarioAutenticado(Long id, String email, Rol rol) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}
