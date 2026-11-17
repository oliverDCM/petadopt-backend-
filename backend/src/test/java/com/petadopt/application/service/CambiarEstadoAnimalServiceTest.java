package com.petadopt.application.service;

import com.petadopt.application.port.in.CambiarEstadoAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.EmailSenderPort;
import com.petadopt.application.port.out.HistorialSolicitudRepositoryPort;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.exception.TransicionEstadoInvalidaException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CambiarEstadoAnimalServiceTest {

    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private SolicitudRepositoryPort solicitudRepository;
    @Mock
    private HistorialSolicitudRepositoryPort historialRepository;
    @Mock
    private EmailSenderPort emailSender;
    @InjectMocks
    private CambiarEstadoAnimalService service;

    private final Usuario adoptante = new Usuario(2L, "Ana", "ana@test.com", "hash", "301", "Cali", Rol.ADOPTANTE);

    private Solicitud solicitud(long id, EstadoSolicitud estado) {
        return new Solicitud(id, 10L, 2L, "Lo cuidaré mucho", estado, LocalDateTime.now().minusDays(1), null);
    }

    private final Usuario vendedor = new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);

    private Animal animal(long vendedorId, EstadoAnimal estado) {
        return new Animal(10L, "Firulais", EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.MACHO,
                "Cariñoso", null, estado, "Bogotá", vendedorId, LocalDateTime.now(),
                false, false, TipoPublicacion.ADOPCION);
    }

    private CambiarEstadoAnimalUseCase.Comando comando(EstadoAnimal nuevo) {
        return new CambiarEstadoAnimalUseCase.Comando("v@test.com", 10L, nuevo);
    }

    @Test
    void el_dueno_marca_como_adoptado_un_animal_reservado() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.RESERVADO)));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal resultado = service.cambiar(comando(EstadoAnimal.ADOPTADO));

        assertThat(resultado.estado()).isEqualTo(EstadoAnimal.ADOPTADO);
        assertThat(resultado.nombre()).isEqualTo("Firulais");
    }

    @Test
    void el_dueno_retira_un_animal_disponible() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.DISPONIBLE)));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.cambiar(comando(EstadoAnimal.RETIRADO)).estado()).isEqualTo(EstadoAnimal.RETIRADO);
    }

    @Test
    void rechaza_una_transicion_no_permitida() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.ADOPTADO)));

        assertThrows(TransicionEstadoInvalidaException.class, () -> service.cambiar(comando(EstadoAnimal.DISPONIBLE)));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_a_quien_no_es_dueno() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(99L, EstadoAnimal.RESERVADO)));

        assertThrows(NoEsDuenoDelAnimalException.class, () -> service.cambiar(comando(EstadoAnimal.ADOPTADO)));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_usuario_inexistente() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.empty());

        assertThrows(NoEsDuenoDelAnimalException.class, () -> service.cambiar(comando(EstadoAnimal.ADOPTADO)));
    }

    @Test
    void rechaza_animal_inexistente() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.empty());

        assertThrows(AnimalNoEncontradoException.class, () -> service.cambiar(comando(EstadoAnimal.ADOPTADO)));
    }

    @Test
    void marcar_como_adoptado_no_toca_las_solicitudes() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.RESERVADO)));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cambiar(comando(EstadoAnimal.ADOPTADO));

        verifyNoInteractions(solicitudRepository, historialRepository, emailSender);
    }

    @Test
    void liberar_la_reserva_cierra_la_solicitud_aprobada_la_deja_en_el_historial_y_avisa_al_adoptante() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.RESERVADO)));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(solicitudRepository.listarActivasPorAnimal(10L)).thenReturn(List.of(solicitud(5L, EstadoSolicitud.APROBADA)));
        when(usuarioRepository.buscarPorId(2L)).thenReturn(Optional.of(adoptante));

        Animal resultado = service.cambiar(comando(EstadoAnimal.DISPONIBLE));

        assertThat(resultado.estado()).isEqualTo(EstadoAnimal.DISPONIBLE);
        ArgumentCaptor<Solicitud> guardada = ArgumentCaptor.forClass(Solicitud.class);
        verify(solicitudRepository).guardar(guardada.capture());
        assertThat(guardada.getValue().id()).isEqualTo(5L);
        assertThat(guardada.getValue().estado()).isEqualTo(EstadoSolicitud.RECHAZADA);
        assertThat(guardada.getValue().fechaDecision()).isNotNull();

        ArgumentCaptor<HistorialSolicitud> cambio = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).registrar(cambio.capture());
        assertThat(cambio.getValue().estadoAnterior()).isEqualTo(EstadoSolicitud.APROBADA);
        assertThat(cambio.getValue().estadoNuevo()).isEqualTo(EstadoSolicitud.RECHAZADA);
        assertThat(cambio.getValue().usuarioId()).isEqualTo(1L);

        verify(emailSender).enviar(eq("ana@test.com"), eq("Tu solicitud por Firulais fue cerrada"), anyString());
    }

    @Test
    void retirar_la_publicacion_cierra_todas_las_solicitudes_pendientes() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.DISPONIBLE)));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(solicitudRepository.listarActivasPorAnimal(10L)).thenReturn(
                List.of(solicitud(5L, EstadoSolicitud.PENDIENTE), solicitud(6L, EstadoSolicitud.PENDIENTE)));
        when(usuarioRepository.buscarPorId(2L)).thenReturn(Optional.of(adoptante));

        service.cambiar(comando(EstadoAnimal.RETIRADO));

        ArgumentCaptor<Solicitud> guardadas = ArgumentCaptor.forClass(Solicitud.class);
        verify(solicitudRepository, times(2)).guardar(guardadas.capture());
        assertThat(guardadas.getAllValues()).extracting(Solicitud::estado)
                .containsExactly(EstadoSolicitud.RECHAZADA, EstadoSolicitud.RECHAZADA);
        verify(historialRepository, times(2)).registrar(any(HistorialSolicitud.class));
        verify(emailSender, times(2)).enviar(eq("ana@test.com"), anyString(), anyString());
    }

    @Test
    void si_el_correo_falla_el_cambio_de_estado_no_se_revierte() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.RESERVADO)));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(solicitudRepository.listarActivasPorAnimal(10L)).thenReturn(List.of(solicitud(5L, EstadoSolicitud.APROBADA)));
        when(usuarioRepository.buscarPorId(2L)).thenReturn(Optional.of(adoptante));
        doThrow(new RuntimeException("smtp caído")).when(emailSender).enviar(anyString(), anyString(), anyString());

        Animal resultado = service.cambiar(comando(EstadoAnimal.RETIRADO));

        assertThat(resultado.estado()).isEqualTo(EstadoAnimal.RETIRADO);
        verify(solicitudRepository).guardar(any(Solicitud.class));
    }
}
