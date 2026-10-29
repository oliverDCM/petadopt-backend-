package com.petadopt.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TokenRecuperacionTest {

    private final LocalDateTime creado = LocalDateTime.of(2026, 10, 28, 10, 0);
    private final TokenRecuperacion token =
            new TokenRecuperacion(1L, 2L, "a".repeat(64), creado, creado.plusMinutes(30), false);

    @Test
    void vence_exactamente_a_los_30_minutos() {
        assertThat(token.estaVencido(creado.plusMinutes(29))).isFalse();
        assertThat(token.estaVencido(creado.plusMinutes(30))).isTrue();
    }

    @Test
    void marcar_usado_conserva_los_demas_datos() {
        TokenRecuperacion usado = token.marcarUsado();
        assertThat(usado.usado()).isTrue();
        assertThat(usado.tokenHash()).isEqualTo(token.tokenHash());
        assertThat(usado.usuarioId()).isEqualTo(2L);
    }
}
