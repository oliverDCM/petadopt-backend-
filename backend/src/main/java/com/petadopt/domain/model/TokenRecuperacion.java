package com.petadopt.domain.model;

import java.time.LocalDateTime;

/**
 * Enlace de recuperación de contraseña. Solo se guarda el hash SHA-256 del token,
 * nunca el token que recibe el usuario por correo.
 */
public record TokenRecuperacion(
        Long id,
        Long usuarioId,
        String tokenHash,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaExpiracion,
        boolean usado
) {
    public boolean estaVencido(LocalDateTime ahora) {
        return !ahora.isBefore(fechaExpiracion);
    }

    public TokenRecuperacion marcarUsado() {
        return new TokenRecuperacion(id, usuarioId, tokenHash, fechaCreacion, fechaExpiracion, true);
    }
}
