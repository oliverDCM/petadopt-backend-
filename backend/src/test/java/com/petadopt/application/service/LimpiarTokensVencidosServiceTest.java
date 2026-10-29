package com.petadopt.application.service;

import com.petadopt.application.port.out.TokenRecuperacionRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LimpiarTokensVencidosServiceTest {

    @Test
    void borra_los_tokens_vencidos_a_la_hora_actual() {
        ZoneId zona = ZoneId.of("America/Bogota");
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 28, 15, 0);
        TokenRecuperacionRepositoryPort repo = mock(TokenRecuperacionRepositoryPort.class);
        when(repo.eliminarVencidos(ahora)).thenReturn(4);

        LimpiarTokensVencidosService service = new LimpiarTokensVencidosService(repo,
                Clock.fixed(ZonedDateTime.of(ahora, zona).toInstant(), zona));

        assertThat(service.limpiar()).isEqualTo(4);
    }
}
