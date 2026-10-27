package com.petadopt.application.service;

import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.RolNoPermitidoException;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarMisSolicitudesServiceTest {

    @Mock
    private SolicitudRepositoryPort solicitudRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @InjectMocks
    private ListarMisSolicitudesService service;

    @Test
    void el_adoptante_ve_sus_propias_solicitudes() {
        Usuario adoptante = new Usuario(2L, "Adoptante", "a@test.com", "hash", "300", "Bogotá", Rol.ADOPTANTE);
        Solicitud solicitud = new Solicitud(5L, 10L, 2L, "Mensaje", EstadoSolicitud.APROBADA,
                LocalDateTime.now(), LocalDateTime.now());
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(adoptante));
        when(solicitudRepository.listarPorSolicitante(2L)).thenReturn(List.of(solicitud));

        assertThat(service.listar("a@test.com")).containsExactly(solicitud);
    }

    @Test
    void rechaza_a_un_vendedor() {
        Usuario vendedor = new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));

        assertThrows(RolNoPermitidoException.class, () -> service.listar("v@test.com"));
    }

    @Test
    void rechaza_a_un_usuario_inexistente() {
        when(usuarioRepository.buscarPorEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThrows(RolNoPermitidoException.class, () -> service.listar("nadie@test.com"));
    }
}