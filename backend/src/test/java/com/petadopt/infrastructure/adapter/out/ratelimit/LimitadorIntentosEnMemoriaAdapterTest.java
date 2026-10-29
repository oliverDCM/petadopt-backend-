package com.petadopt.infrastructure.adapter.out.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LimitadorIntentosEnMemoriaAdapterTest {

    /** Reloj que se puede adelantar a mano. */
    private static final class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-10-28T20:00:00Z");

        void avanzar(Duration d) {
            ahora = ahora.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private final RelojManual reloj = new RelojManual();
    private final LimitadorIntentosEnMemoriaAdapter limitador = new LimitadorIntentosEnMemoriaAdapter(3, 60, reloj);

    @Test
    void permite_tres_intentos_por_hora_y_bloquea_el_cuarto() {
        assertThat(limitador.intentar("email:ana@test.com")).isTrue();
        assertThat(limitador.intentar("email:ana@test.com")).isTrue();
        assertThat(limitador.intentar("email:ana@test.com")).isTrue();
        assertThat(limitador.intentar("email:ana@test.com")).isFalse();
    }

    @Test
    void las_claves_se_cuentan_por_separado() {
        for (int i = 0; i < 3; i++) {
            limitador.intentar("email:ana@test.com");
        }
        assertThat(limitador.intentar("email:otro@test.com")).isTrue();
        assertThat(limitador.intentar("ip:10.0.0.1")).isTrue();
    }

    @Test
    void despues_de_la_ventana_vuelve_a_permitir() {
        for (int i = 0; i < 3; i++) {
            limitador.intentar("ip:10.0.0.1");
        }
        reloj.avanzar(Duration.ofMinutes(61));
        assertThat(limitador.intentar("ip:10.0.0.1")).isTrue();
    }

    @Test
    void purgar_libera_las_claves_vencidas() {
        limitador.intentar("email:ana@test.com");
        limitador.intentar("email:otro@test.com");
        reloj.avanzar(Duration.ofMinutes(30));
        limitador.intentar("email:reciente@test.com");
        reloj.avanzar(Duration.ofMinutes(31));

        limitador.purgar();

        assertThat(limitador.clavesRegistradas()).isEqualTo(1);
    }
}
