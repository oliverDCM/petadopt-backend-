package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String ciudad,
        Rol rol
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.id(), u.nombre(), u.email(),
                u.telefono(), u.ciudad(), u.rol());
    }
}