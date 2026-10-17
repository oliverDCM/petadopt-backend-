package com.petadopt.application.port.out;

import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;

import java.util.Optional;

/**
 * Puerto de salida para emitir y validar tokens de acceso. El núcleo no conoce
 * JWT ni ninguna librería concreta; eso lo resuelve el adaptador.
 */
public interface TokenServicePort {

    String generar(Usuario usuario);

    /** Devuelve los datos del token si es válido (firma correcta y no vencido). */
    Optional<DatosToken> validar(String token);

    record DatosToken(Long usuarioId, String email, Rol rol) {
    }
}
