package com.petadopt.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlantillasCorreoTest {

    @Test
    void aprobada_para_adoptante_incluye_el_contacto_del_vendedor() {
        PlantillasCorreo.Correo c = PlantillasCorreo.solicitudAprobadaParaAdoptante(
                "Ana", "Firulais", "Carlos", "carlos@test.com", null, "Fusagasugá");
        assertThat(c.asunto()).contains("Firulais");
        assertThat(c.cuerpo()).contains("Ana", "Carlos", "carlos@test.com", "(no registrado)", "Fusagasugá");
    }

    @Test
    void aprobada_para_vendedor_incluye_el_contacto_del_adoptante() {
        PlantillasCorreo.Correo c = PlantillasCorreo.solicitudAprobadaParaVendedor(
                "Carlos", "Firulais", "Ana", "ana@test.com", "3001234567", "Bogotá");
        assertThat(c.cuerpo()).contains("Carlos", "Ana", "ana@test.com", "3001234567", "ADOPTADO");
    }

    @Test
    void recuperacion_incluye_el_enlace_y_la_vigencia() {
        PlantillasCorreo.Correo c = PlantillasCorreo.recuperacionContrasena("Ana", "http://x/restablecer?token=t", 30);
        assertThat(c.cuerpo()).contains("http://x/restablecer?token=t", "30 minutos");
    }

    @Test
    void contrasena_cambiada_saluda_al_usuario() {
        assertThat(PlantillasCorreo.contrasenaCambiada("Ana").cuerpo()).contains("Hola Ana");
    }

    @Test
    void el_asunto_no_puede_partirse_con_saltos_de_linea_escritos_por_un_usuario() {
        var correo = PlantillasCorreo.solicitudAprobadaParaAdoptante("Ana", "Firu\r\nBcc: otro@test.com\n2026 ERROR falso",
                "Carlos", "carlos@test.com", null, null);

        assertThat(correo.asunto()).doesNotContain("\r").doesNotContain("\n");
        assertThat(correo.asunto()).isEqualTo("Tu solicitud por Firu Bcc: otro@test.com 2026 ERROR falso fue aprobada");
    }

    @Test
    void solicitud_cerrada_explica_si_se_cancelo_la_reserva_o_se_retiro_la_publicacion() {
        var reserva = PlantillasCorreo.solicitudCerrada("Ana", "Firulais", true);
        var retiro = PlantillasCorreo.solicitudCerrada("Ana", "Firulais", false);

        assertThat(reserva.asunto()).isEqualTo("Tu solicitud por Firulais fue cerrada");
        assertThat(reserva.cuerpo()).contains("Hola Ana").contains("canceló la reserva");
        assertThat(retiro.cuerpo()).contains("retiró la publicación");
    }
}
