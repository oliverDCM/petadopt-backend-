package com.petadopt.application.service;

import com.petadopt.domain.exception.ContrasenaInvalidaException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReglasContrasenaTest {

    @Test
    void acepta_contrasenas_de_8_caracteres_a_72_bytes() {
        assertThatCode(() -> ReglasContrasena.validar("12345678")).doesNotThrowAnyException();
        assertThatCode(() -> ReglasContrasena.validar("a".repeat(72))).doesNotThrowAnyException();
        assertThatCode(() -> ReglasContrasena.validar("ñ".repeat(36))).doesNotThrowAnyException(); // 36 x 2 bytes
    }

    @Test
    void rechaza_contrasenas_cortas_o_nulas() {
        assertThatThrownBy(() -> ReglasContrasena.validar("1234567")).isInstanceOf(ContrasenaInvalidaException.class);
        assertThatThrownBy(() -> ReglasContrasena.validar(null)).isInstanceOf(ContrasenaInvalidaException.class);
    }

    @Test
    void cuenta_bytes_y_no_caracteres_porque_bcrypt_solo_procesa_72_bytes() {
        // 37 "ñ" son 37 caracteres pero 74 bytes: BCrypt los rechazaría con un error 500
        assertThatThrownBy(() -> ReglasContrasena.validar("ñ".repeat(37)))
                .isInstanceOf(ContrasenaInvalidaException.class)
                .hasMessageContaining("demasiado larga");
        assertThatThrownBy(() -> ReglasContrasena.validar("a".repeat(73))).isInstanceOf(ContrasenaInvalidaException.class);
    }
}
