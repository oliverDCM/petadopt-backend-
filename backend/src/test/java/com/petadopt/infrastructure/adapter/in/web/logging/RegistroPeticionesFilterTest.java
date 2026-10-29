package com.petadopt.infrastructure.adapter.in.web.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistroPeticionesFilterTest {

    private final RegistroPeticionesFilter filtro = new RegistroPeticionesFilter();

    @Test
    void agrega_el_request_id_a_la_respuesta_y_al_mdc_durante_la_peticion() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/animales");
        MockHttpServletResponse res = new MockHttpServletResponse();
        String[] idDuranteLaPeticion = new String[1];
        FilterChain cadena = (rq, rs) -> idDuranteLaPeticion[0] = MDC.get(RegistroPeticionesFilter.CLAVE_MDC);

        filtro.doFilter(req, res, cadena);

        String id = res.getHeader(RegistroPeticionesFilter.CABECERA_ID);
        assertThat(id).hasSize(8);
        assertThat(idDuranteLaPeticion[0]).isEqualTo(id);
        assertThat(MDC.get(RegistroPeticionesFilter.CLAVE_MDC)).isNull();
    }

    @Test
    void limpia_el_mdc_aunque_la_peticion_falle() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/solicitudes");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain cadena = (rq, rs) -> {
            throw new IllegalStateException("fallo");
        };

        assertThrows(IllegalStateException.class, () -> filtro.doFilter(req, res, cadena));
        assertThat(MDC.get(RegistroPeticionesFilter.CLAVE_MDC)).isNull();
    }

    @Test
    void las_rutas_de_salud_tambien_pasan_por_el_filtro() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse res = new MockHttpServletResponse();
        boolean[] llamado = {false};

        filtro.doFilter(req, res, (rq, rs) -> llamado[0] = true);

        assertThat(llamado[0]).isTrue();
        assertThat(res.getHeader(RegistroPeticionesFilter.CABECERA_ID)).isNotNull();
    }
}
