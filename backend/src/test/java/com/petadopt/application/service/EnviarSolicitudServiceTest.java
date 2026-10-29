package com.petadopt.application.service;

import com.petadopt.application.port.in.EnviarSolicitudUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.HistorialSolicitudRepositoryPort;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoDisponibleException;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.SoloAdoptantePuedeSolicitarException;
import com.petadopt.domain.exception.SolicitudDuplicadaException;
import com.petadopt.domain.exception.SolicitudPropioAnimalException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.HistorialSolicitud;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.domain.model.TipoPublicacion;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnviarSolicitudServiceTest {

    @Mock
    private SolicitudRepositoryPort solicitudRepository;
    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private HistorialSolicitudRepositoryPort historialRepository;
    @InjectMocks
    private EnviarSolicitudService service;

    private Usuario adoptante(long id) {
        return new Usuario(id, "Adoptante", "a@test.com", "hash", "300", "Bogotá", Rol.ADOPTANTE);
    }

    private Animal animal(long id, long vendedorId, EstadoAnimal estado) {
        return new Animal(id, "Firulais", EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.MACHO,
                "Cariñoso", null, estado, "Bogotá", vendedorId, LocalDateTime.now(),
                false, false, TipoPublicacion.ADOPCION);
    }

    private EnviarSolicitudUseCase.Comando comando(long animalId) {
        return new EnviarSolicitudUseCase.Comando("a@test.com", animalId, "  Quiero adoptarlo  ");
    }

    @Test
    void envia_solicitud_pendiente() {
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(adoptante(2L)));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(10L, 1L, EstadoAnimal.DISPONIBLE)));
        when(solicitudRepository.guardar(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud resultado = service.enviar(comando(10L));

        assertThat(resultado.estado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(resultado.animalId()).isEqualTo(10L);
        assertThat(resultado.solicitanteId()).isEqualTo(2L);
        assertThat(resultado.mensaje()).isEqualTo("Quiero adoptarlo");

        ArgumentCaptor<HistorialSolicitud> historial = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).registrar(historial.capture());
        assertThat(historial.getValue().estadoAnterior()).isNull();
        assertThat(historial.getValue().estadoNuevo()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(historial.getValue().usuarioId()).isEqualTo(2L);
    }

    @Test
    void rechaza_usuario_que_no_es_adoptante() {
        Usuario vendedor = new Usuario(1L, "Vendedor", "a@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(vendedor));

        assertThrows(SoloAdoptantePuedeSolicitarException.class, () -> service.enviar(comando(10L)));
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    void rechaza_animal_inexistente() {
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(adoptante(2L)));
        when(animalRepository.buscarPorIdParaActualizar(99L)).thenReturn(Optional.empty());

        assertThrows(AnimalNoEncontradoException.class, () -> service.enviar(comando(99L)));
    }

    @Test
    void rechaza_solicitud_sobre_animal_propio() {
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(adoptante(1L)));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(10L, 1L, EstadoAnimal.DISPONIBLE)));

        assertThrows(SolicitudPropioAnimalException.class, () -> service.enviar(comando(10L)));
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    void rechaza_animal_no_disponible() {
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(adoptante(2L)));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(10L, 1L, EstadoAnimal.RESERVADO)));

        assertThrows(AnimalNoDisponibleException.class, () -> service.enviar(comando(10L)));
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    void rechaza_solicitud_duplicada_activa_sobre_el_mismo_animal() {
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(adoptante(2L)));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(10L, 1L, EstadoAnimal.DISPONIBLE)));
        when(solicitudRepository.existeActivaPorAnimalYSolicitante(10L, 2L)).thenReturn(true);

        assertThrows(SolicitudDuplicadaException.class, () -> service.enviar(comando(10L)));
        verify(solicitudRepository, never()).guardar(any());
        verify(historialRepository, never()).registrar(any());
    }
}