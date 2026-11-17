package com.petadopt.application.service;

import com.petadopt.domain.exception.ContrasenaInvalidaException;

import java.nio.charset.StandardCharsets;

/** Reglas de la contraseña compartidas por el registro y el restablecimiento. */
final class ReglasContrasena {

    static final int LONGITUD_MINIMA = 8;
    /** BCrypt solo procesa 72 bytes: una letra con tilde o una ñ ocupan 2 y un emoji 4. */
    static final int BYTES_MAXIMOS = 72;

    private ReglasContrasena() {
    }

    static void validar(String password) {
        if (password == null || password.length() < LONGITUD_MINIMA) {
            throw new ContrasenaInvalidaException();
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS) {
            throw new ContrasenaInvalidaException(
                    "La contraseña es demasiado larga: usa como máximo 72 caracteres "
                            + "(las tildes, la ñ y los emojis cuentan por dos o más)");
        }
    }
}
