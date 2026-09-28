package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;

public record LoginResponse(String token, Rol rol, String nombre, String email) {

    public static LoginResponse desde(String token, Usuario u) {
        return new LoginResponse(token, u.rol(), u.nombre(), u.email());
    }
}