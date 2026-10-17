package com.petadopt.infrastructure.adapter.in.web.security;

import com.petadopt.domain.model.Rol;

/** Principal que queda en el SecurityContext tras validar el token. */
public record UsuarioAutenticado(Long id, String email, Rol rol) {
}
