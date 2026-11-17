package com.petadopt.infrastructure.adapter.out.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ProteccionFuerzaBrutaEnMemoriaAdapterTest {

    /** Reloj que la prueba adelanta a mano. */
    private static final class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-11-16T15:00:00Z");

        void avanzar(Duration tiempo) {
            ahora = ahora.plus(tiempo);
        }

        @Override
        public Instant instant() {
            return ahora;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private final RelojManual reloj = new RelojManual();
    private final ProteccionFuerzaBrutaEnMemoriaAdapter proteccion = new ProteccionFuerzaBrutaEnMemoriaAdapter(15, reloj);

    @Test
    void una_clave_sin_fallos_no_supera_el_limite() {
        assertThat(proteccion.superaLimite("ip:1.2.3.4", 5)).isFalse();
        assertThat(proteccion.clavesRegistradas()).isZero(); // consultar no ocupa memoria
    }

    @Test
    void supera_el_limite_al_llegar_al_maximo_de_fallos() {
        for (int i = 0; i < 4; i++) {
            proteccion.registrarFallo("cuenta:ana@test.com|1.2.3.4");
        }
        assertThat(proteccion.superaLimite("cuenta:ana@test.com|1.2.3.4", 5)).isFalse();

        proteccion.registrarFallo("cuenta:ana@test.com|1.2.3.4");

        assertThat(proteccion.superaLimite("cuenta:ana@test.com|1.2.3.4", 5)).isTrue();
        assertThat(proteccion.superaLimite("cuenta:ana@test.com|9.9.9.9", 5)).isFalse(); // otra IP no se ve afectada
    }

    @Test
    void los_fallos_caducan_al_pasar_la_ventana() {
        for (int i = 0; i < 5; i++) {
            proteccion.registrarFallo("ip:1.2.3.4");
        }
        assertThat(proteccion.superaLimite("ip:1.2.3.4", 5)).isTrue();

        reloj.avanzar(Duration.ofMinutes(15));

        assertThat(proteccion.superaLimite("ip:1.2.3.4", 5)).isFalse();
    }

    @Test
    void olvidar_borra_los_fallos_de_la_clave() {
        for (int i = 0; i < 5; i++) {
            proteccion.registrarFallo("cuenta:ana@test.com|1.2.3.4");
        }

        proteccion.olvidar("cuenta:ana@test.com|1.2.3.4");

        assertThat(proteccion.superaLimite("cuenta:ana@test.com|1.2.3.4", 5)).isFalse();
        assertThat(proteccion.clavesRegistradas()).isZero();
    }

    @Test
    void purgar_libera_las_claves_caducadas_y_conserva_las_vigentes() {
        proteccion.registrarFallo("ip:vieja");
        reloj.avanzar(Duration.ofMinutes(10));
        proteccion.registrarFallo("ip:reciente");
        reloj.avanzar(Duration.ofMinutes(6)); // "vieja" lleva 16 min; "reciente", 6

        proteccion.purgar();

        assertThat(proteccion.clavesRegistradas()).isEqualTo(1);
        assertThat(proteccion.superaLimite("ip:reciente", 1)).isTrue();
    }

    @Test
    void cada_clave_guarda_un_numero_acotado_de_fallos() {
        for (int i = 0; i < ProteccionFuerzaBrutaEnMemoriaAdapter.MAX_FALLOS_POR_CLAVE + 500; i++) {
            proteccion.registrarFallo("ip:1.2.3.4");
        }

        assertThat(proteccion.superaLimite("ip:1.2.3.4", ProteccionFuerzaBrutaEnMemoriaAdapter.MAX_FALLOS_POR_CLAVE)).isTrue();
        assertThat(proteccion.superaLimite("ip:1.2.3.4", ProteccionFuerzaBrutaEnMemoriaAdapter.MAX_FALLOS_POR_CLAVE + 1)).isFalse();
    }
}
