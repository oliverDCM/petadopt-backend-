package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.PerfilPublico;
import com.petadopt.domain.model.Rol;

public record PerfilPublicoResponse(String nombre, String ciudad, Rol rol) {

    public static PerfilPublicoResponse desde(PerfilPublico perfil) {
        return new PerfilPublicoResponse(perfil.nombre(), perfil.ciudad(), perfil.rol());
    }
}