package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.Rol;

/** Misma forma que la interfaz Sesion del frontend: token, rol, nombre y email. */
public record LoginResponse(
        String token,
        Rol rol,
        String nombre,
        String email
) {
}
