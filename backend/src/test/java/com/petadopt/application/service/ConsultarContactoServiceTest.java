package com.petadopt.application.service;

import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.ContactoNoDisponibleException;
import com.petadopt.domain.exception.SolicitudNoEncontradaException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.Contacto;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarContactoServiceTest {

    @Mock
    private SolicitudRepositoryPort solicitudRepository;
    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @InjectMocks
    private ConsultarContactoService service;

    private final Usuario vendedor = new Usuario(1L, "Carlos", "carlos@test.com", "h", "3109876543", "Fusagasugá", Rol.VENDEDOR);
    private final Usuario adoptante = new Usuario(2L, "Ana", "ana@test.com", "h", "3001234567", "Bogotá", Rol.ADOPTANTE);
    private final Usuario ajeno = new Usuario(3L, "Otro", "otro@test.com", "h", null, null, Rol.ADOPTANTE);

    private final Animal animal = new Animal(10L, "Firulais", EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.MACHO,
            "Cariñoso", null, EstadoAnimal.RESERVADO, "Fusagasugá", 1L, LocalDateTime.now(),
            false, false, TipoPublicacion.ADOPCION);

    private void preparar(Usuario quien, EstadoSolicitud estado) {
        when(usuarioRepository.buscarPorEmail(quien.email())).thenReturn(Optional.of(quien));
        when(solicitudRepository.buscarPorId(5L)).thenReturn(Optional.of(
                new Solicitud(5L, 10L, 2L, "Hola", estado, LocalDateTime.now(), LocalDateTime.now())));
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.of(animal));
    }

    @Test
    void el_adoptante_ve_el_contacto_del_vendedor() {
        preparar(adoptante, EstadoSolicitud.APROBADA);
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(vendedor));

        Contacto c = service.consultar("ana@test.com", 5L);

        assertThat(c.nombre()).isEqualTo("Carlos");
        assertThat(c.email()).isEqualTo("carlos@test.com");
        assertThat(c.telefono()).isEqualTo("3109876543");
        assertThat(c.ciudad()).isEqualTo("Fusagasugá");
    }

    @Test
    void el_vendedor_ve_el_contacto_del_adoptante() {
        preparar(vendedor, EstadoSolicitud.APROBADA);
        when(usuarioRepository.buscarPorId(2L)).thenReturn(Optional.of(adoptante));

        Contacto c = service.consultar("carlos@test.com", 5L);

        assertThat(c.nombre()).isEqualTo("Ana");
        assertThat(c.email()).isEqualTo("ana@test.com");
    }

    @Test
    void solicitud_pendiente_devuelve_403() {
        preparar(adoptante, EstadoSolicitud.PENDIENTE);
        assertThrows(ContactoNoDisponibleException.class, () -> service.consultar("ana@test.com", 5L));
    }

    @Test
    void solicitud_rechazada_devuelve_403() {
        preparar(vendedor, EstadoSolicitud.RECHAZADA);
        assertThrows(ContactoNoDisponibleException.class, () -> service.consultar("carlos@test.com", 5L));
    }

    @Test
    void usuario_ajeno_devuelve_403_aunque_este_aprobada() {
        preparar(ajeno, EstadoSolicitud.APROBADA);
        assertThrows(ContactoNoDisponibleException.class, () -> service.consultar("otro@test.com", 5L));
    }

    @Test
    void solicitud_inexistente_devuelve_404() {
        when(usuarioRepository.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(adoptante));
        when(solicitudRepository.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(SolicitudNoEncontradaException.class, () -> service.consultar("ana@test.com", 99L));
    }
}
