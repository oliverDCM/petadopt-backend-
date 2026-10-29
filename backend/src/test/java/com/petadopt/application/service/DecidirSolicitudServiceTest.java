package com.petadopt.application.service;

import com.petadopt.application.port.in.DecidirSolicitudUseCase;
import com.petadopt.application.port.in.DecidirSolicitudUseCase.Decision;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoDisponibleException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.exception.SolicitudNoEncontradaException;
import com.petadopt.domain.exception.SolicitudYaDecididaException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.EstadoSolicitud;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecidirSolicitudServiceTest {

    @Mock
    private SolicitudRepositoryPort solicitudRepository;
    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @InjectMocks
    private DecidirSolicitudService service;

    private final Usuario vendedor =
            new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);

    private Animal animal(long vendedorId) {
        return new Animal(10L, "Firulais", EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.MACHO,
                "Cariñoso", null, EstadoAnimal.DISPONIBLE, "Bogotá", vendedorId, LocalDateTime.now(),
                false, false, TipoPublicacion.ADOPCION);
    }

    private Solicitud solicitud(long id, EstadoSolicitud estado) {
        return new Solicitud(id, 10L, 2L, "Mensaje", estado, LocalDateTime.now(), null);
    }

    private DecidirSolicitudUseCase.Comando comando(long solicitudId, Decision decision) {
        return new DecidirSolicitudUseCase.Comando("v@test.com", solicitudId, decision);
    }

    @Test
    void aprobar_reserva_el_animal_y_rechaza_las_demas_pendientes() {
        Solicitud principal = solicitud(5L, EstadoSolicitud.PENDIENTE);
        Solicitud otra = solicitud(6L, EstadoSolicitud.PENDIENTE);
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(solicitudRepository.buscarPorId(5L)).thenReturn(Optional.of(principal));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L)));
        when(solicitudRepository.guardar(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));
        when(solicitudRepository.listarPendientesPorAnimal(10L)).thenReturn(List.of(principal, otra));

        Solicitud resultado = service.decidir(comando(5L, Decision.APROBAR));

        assertThat(resultado.estado()).isEqualTo(EstadoSolicitud.APROBADA);
        assertThat(resultado.fechaDecision()).isNotNull();

        ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
        verify(animalRepository).guardar(animalCaptor.capture());
        assertThat(animalCaptor.getValue().estado()).isEqualTo(EstadoAnimal.RESERVADO);

        ArgumentCaptor<Solicitud> solicitudCaptor = ArgumentCaptor.forClass(Solicitud.class);
        verify(solicitudRepository, times(2)).guardar(solicitudCaptor.capture());
        Solicitud rechazada = solicitudCaptor.getAllValues().get(1);
        assertThat(rechazada.id()).isEqualTo(6L);
        assertThat(rechazada.estado()).isEqualTo(EstadoSolicitud.RECHAZADA);
    }

    @Test
    void rechazar_no_cambia_el_estado_del_animal() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(solicitudRepository.buscarPorId(5L)).thenReturn(Optional.of(solicitud(5L, EstadoSolicitud.PENDIENTE)));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L)));
        when(solicitudRepository.guardar(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud resultado = service.decidir(comando(5L, Decision.RECHAZAR));

        assertThat(resultado.estado()).isEqualTo(EstadoSolicitud.RECHAZADA);
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_decision_de_quien_no_es_dueno_del_animal() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(solicitudRepository.buscarPorId(5L)).thenReturn(Optional.of(solicitud(5L, EstadoSolicitud.PENDIENTE)));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(99L)));

        assertThrows(NoEsDuenoDelAnimalException.class, () -> service.decidir(comando(5L, Decision.APROBAR)));
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    void rechaza_solicitud_ya_decidida() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(solicitudRepository.buscarPorId(5L)).thenReturn(Optional.of(solicitud(5L, EstadoSolicitud.APROBADA)));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L)));

        assertThrows(SolicitudYaDecididaException.class, () -> service.decidir(comando(5L, Decision.APROBAR)));
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    void rechaza_solicitud_inexistente() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(solicitudRepository.buscarPorId(77L)).thenReturn(Optional.empty());

        assertThrows(SolicitudNoEncontradaException.class, () -> service.decidir(comando(77L, Decision.APROBAR)));
    }

    @Test
    void no_aprueba_si_el_animal_ya_no_esta_disponible() {
        Animal reservado = animal(1L).conEstado(EstadoAnimal.RESERVADO);
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(solicitudRepository.buscarPorId(5L)).thenReturn(Optional.of(solicitud(5L, EstadoSolicitud.PENDIENTE)));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(reservado));

        assertThrows(AnimalNoDisponibleException.class, () -> service.decidir(comando(5L, Decision.APROBAR)));
        verify(solicitudRepository, never()).guardar(any());
        verify(animalRepository, never()).guardar(any());
    }
}
