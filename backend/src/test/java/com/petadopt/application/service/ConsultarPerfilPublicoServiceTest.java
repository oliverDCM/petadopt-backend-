package com.petadopt.application.service;

import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.PerfilNoAccesibleException;
import com.petadopt.domain.exception.RolNoPermitidoException;
import com.petadopt.domain.exception.UsuarioNoEncontradoException;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.PerfilPublico;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarPerfilPublicoServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private SolicitudRepositoryPort solicitudRepository;
    @InjectMocks
    private ConsultarPerfilPublicoService service;

    private final Usuario vendedor =
            new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);
    private final Usuario adoptante =
            new Usuario(2L, "Ana Pérez", "ana@test.com", "hash-secreto", "3001234567", "Cali", Rol.ADOPTANTE);

    private Solicitud solicitudDe(long solicitanteId) {
        return new Solicitud(5L, 10L, solicitanteId, "Mensaje", EstadoSolicitud.PENDIENTE,
                LocalDateTime.now(), null);
    }

    @Test
    void devuelve_solo_nombre_ciudad_y_rol_del_solicitante() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(usuarioRepository.buscarPorId(2L)).thenReturn(Optional.of(adoptante));
        when(solicitudRepository.listarPorVendedor(1L)).thenReturn(List.of(solicitudDe(2L)));

        PerfilPublico perfil = service.consultar("v@test.com", 2L);

        assertThat(perfil).isEqualTo(new PerfilPublico("Ana Pérez", "Cali", Rol.ADOPTANTE));
    }

    @Test
    void rechaza_si_el_usuario_no_envio_solicitud_sobre_animales_del_vendedor() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(usuarioRepository.buscarPorId(2L)).thenReturn(Optional.of(adoptante));
        when(solicitudRepository.listarPorVendedor(1L)).thenReturn(List.of(solicitudDe(3L)));

        assertThrows(PerfilNoAccesibleException.class, () -> service.consultar("v@test.com", 2L));
    }

    @Test
    void rechaza_usuario_inexistente() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(usuarioRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> service.consultar("v@test.com", 99L));
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void rechaza_a_quien_no_es_vendedor() {
        when(usuarioRepository.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(adoptante));

        assertThrows(RolNoPermitidoException.class, () -> service.consultar("ana@test.com", 2L));
        verify(usuarioRepository, never()).buscarPorId(any());
    }
}
