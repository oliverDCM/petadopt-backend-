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
}
