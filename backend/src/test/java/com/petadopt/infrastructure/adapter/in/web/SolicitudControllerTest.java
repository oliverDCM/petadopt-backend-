package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.ConsultarContactoUseCase;
import com.petadopt.application.port.in.DecidirSolicitudUseCase;
import com.petadopt.application.port.in.EnviarSolicitudUseCase;
import com.petadopt.application.port.in.ListarMisSolicitudesUseCase;
import com.petadopt.application.port.in.ListarSolicitudesRecibidasUseCase;
import com.petadopt.domain.model.Contacto;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.infrastructure.adapter.in.web.dto.ContactoResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.DecisionRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.SolicitudRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.SolicitudResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SolicitudControllerTest {

    private final EnviarSolicitudUseCase enviar = mock(EnviarSolicitudUseCase.class);
    private final DecidirSolicitudUseCase decidir = mock(DecidirSolicitudUseCase.class);
    private final ListarSolicitudesRecibidasUseCase recibidas = mock(ListarSolicitudesRecibidasUseCase.class);
    private final ListarMisSolicitudesUseCase mias = mock(ListarMisSolicitudesUseCase.class);
    private final ConsultarContactoUseCase contacto = mock(ConsultarContactoUseCase.class);

    private final SolicitudController controller = new SolicitudController(enviar, decidir, recibidas, mias, contacto);
    private final Authentication usuario = new UsernamePasswordAuthenticationToken("a@test.com", null);

    private Solicitud solicitud(EstadoSolicitud estado) {
        return new Solicitud(5L, 10L, 2L, "Hola", estado, LocalDateTime.now(), null);
    }

    @Test
    void enviar_responde_201() {
        when(enviar.enviar(new EnviarSolicitudUseCase.Comando("a@test.com", 10L, "Hola"))).thenReturn(solicitud(EstadoSolicitud.PENDIENTE));

        ResponseEntity<SolicitudResponse> r = controller.enviar(new SolicitudRequest(10L, "Hola"), usuario);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(r.getBody().estado()).isEqualTo(EstadoSolicitud.PENDIENTE);
    }

    @Test
    void decidir_devuelve_la_solicitud_actualizada() {
        when(decidir.decidir(new DecidirSolicitudUseCase.Comando("a@test.com", 5L, DecidirSolicitudUseCase.Decision.APROBAR)))
                .thenReturn(solicitud(EstadoSolicitud.APROBADA));

        SolicitudResponse r = controller.decidir(5L, new DecisionRequest(DecidirSolicitudUseCase.Decision.APROBAR), usuario);

        assertThat(r.estado()).isEqualTo(EstadoSolicitud.APROBADA);
    }

    @Test
    void lista_recibidas_y_mias() {
        when(recibidas.listar("a@test.com")).thenReturn(List.of(solicitud(EstadoSolicitud.PENDIENTE)));
        when(mias.listar("a@test.com")).thenReturn(List.of(solicitud(EstadoSolicitud.RECHAZADA), solicitud(EstadoSolicitud.APROBADA)));

        assertThat(controller.recibidas(usuario)).hasSize(1);
        assertThat(controller.mias(usuario)).hasSize(2);
    }

    @Test
    void contacto_devuelve_los_datos_de_la_otra_parte() {
        when(contacto.consultar("a@test.com", 5L)).thenReturn(new Contacto("Carlos", "Fusagasugá", "c@test.com", "310"));

        ContactoResponse r = controller.contacto(5L, usuario);

        assertThat(r.nombre()).isEqualTo("Carlos");
        assertThat(r.email()).isEqualTo("c@test.com");
        assertThat(r.telefono()).isEqualTo("310");
    }
}
