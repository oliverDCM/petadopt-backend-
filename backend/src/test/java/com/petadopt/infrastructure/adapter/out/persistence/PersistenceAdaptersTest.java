package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.HistorialSolicitud;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.domain.model.TipoPublicacion;
import com.petadopt.domain.model.TokenRecuperacion;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Los adaptadores traducen entre el dominio y las entidades JPA; los repositorios se simulan. */
class PersistenceAdaptersTest {

    private final UsuarioJpaRepository usuarioRepo = mock(UsuarioJpaRepository.class);
    private final AnimalJpaRepository animalRepo = mock(AnimalJpaRepository.class);
    private final SolicitudJpaRepository solicitudRepo = mock(SolicitudJpaRepository.class);

    private final UsuarioEntity vendedorEntity =
            new UsuarioEntity(1L, "Carlos", "c@test.com", "hash", "310", "Fusagasugá", Rol.VENDEDOR);
    private final UsuarioEntity adoptanteEntity =
            new UsuarioEntity(2L, "Ana", "a@test.com", "hash", "300", "Bogotá", Rol.ADOPTANTE);
    private final LocalDateTime fecha = LocalDateTime.of(2026, 10, 1, 10, 0);
    private final AnimalEntity animalEntity = new AnimalEntity(10L, "Lupe", EspecieAnimal.PERRO, "Criolla", 14,
            SexoAnimal.HEMBRA, "Juguetona", null, EstadoAnimal.DISPONIBLE, "Fusagasugá", vendedorEntity, fecha,
            true, false, TipoPublicacion.ADOPCION);

    // ---------- Usuarios ----------

    @Test
    void usuario_se_guarda_y_se_busca() {
        UsuarioPersistenceAdapter adapter = new UsuarioPersistenceAdapter(usuarioRepo);
        when(usuarioRepo.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepo.findByEmail("c@test.com")).thenReturn(Optional.of(vendedorEntity));
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(vendedorEntity));
        when(usuarioRepo.existsByEmail("c@test.com")).thenReturn(true);

        Usuario guardado = adapter.guardar(new Usuario(null, "Ana", "a@test.com", "hash", null, null, Rol.ADOPTANTE));

        assertThat(guardado.email()).isEqualTo("a@test.com");
        assertThat(adapter.buscarPorEmail("c@test.com")).map(Usuario::nombre).contains("Carlos");
        assertThat(adapter.buscarPorId(1L)).map(Usuario::rol).contains(Rol.VENDEDOR);
        assertThat(adapter.existePorEmail("c@test.com")).isTrue();
    }

    // ---------- Animales ----------

    @Test
    void animal_se_convierte_con_valores_por_defecto() {
        AnimalPersistenceAdapter adapter = new AnimalPersistenceAdapter(animalRepo, usuarioRepo);
        when(usuarioRepo.getReferenceById(1L)).thenReturn(vendedorEntity);
        when(animalRepo.save(any(AnimalEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal nuevo = new Animal(null, "Rocky", EspecieAnimal.GATO, null, null, SexoAnimal.MACHO, null, null,
                null, null, 1L, null, false, false, null);
        Animal guardado = adapter.guardar(nuevo);

        assertThat(guardado.estado()).isEqualTo(EstadoAnimal.DISPONIBLE);
        assertThat(guardado.tipo()).isEqualTo(TipoPublicacion.ADOPCION);
        assertThat(guardado.fechaPublicacion()).isNotNull();
        assertThat(guardado.vendedorId()).isEqualTo(1L);
    }

    @Test
    void animal_se_consulta_de_varias_formas() {
        AnimalPersistenceAdapter adapter = new AnimalPersistenceAdapter(animalRepo, usuarioRepo);
        when(animalRepo.findById(10L)).thenReturn(Optional.of(animalEntity));
        when(animalRepo.buscarBloqueadoPorId(10L)).thenReturn(Optional.of(animalEntity));
        when(animalRepo.findByEstado(EstadoAnimal.DISPONIBLE)).thenReturn(List.of(animalEntity));
        when(animalRepo.findByVendedorId(1L)).thenReturn(List.of(animalEntity));

        assertThat(adapter.buscarPorId(10L)).map(Animal::nombre).contains("Lupe");
        assertThat(adapter.buscarPorIdParaActualizar(10L)).isPresent();
        assertThat(adapter.listarPorEstado(EstadoAnimal.DISPONIBLE)).hasSize(1);
        assertThat(adapter.listarPorVendedor(1L)).extracting(Animal::vacunado).containsExactly(true);
    }

    // ---------- Solicitudes ----------

    private SolicitudEntity solicitudEntity(EstadoSolicitud estado) {
        return new SolicitudEntity(5L, animalEntity, adoptanteEntity, "Hola", estado, fecha, null);
    }

    @Test
    void solicitud_nueva_se_guarda_pendiente() {
        SolicitudPersistenceAdapter adapter = new SolicitudPersistenceAdapter(solicitudRepo, animalRepo, usuarioRepo);
        when(animalRepo.getReferenceById(10L)).thenReturn(animalEntity);
        when(usuarioRepo.getReferenceById(2L)).thenReturn(adoptanteEntity);
        when(solicitudRepo.save(any(SolicitudEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud guardada = adapter.guardar(new Solicitud(null, 10L, 2L, "Hola", null, null, null));

        assertThat(guardada.estado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(guardada.fechaSolicitud()).isNotNull();
        assertThat(guardada.animalId()).isEqualTo(10L);
        assertThat(guardada.solicitanteId()).isEqualTo(2L);
    }

    @Test
    void solicitudes_se_consultan_por_solicitante_vendedor_y_animal() {
        SolicitudPersistenceAdapter adapter = new SolicitudPersistenceAdapter(solicitudRepo, animalRepo, usuarioRepo);
        when(solicitudRepo.findById(5L)).thenReturn(Optional.of(solicitudEntity(EstadoSolicitud.PENDIENTE)));
        when(solicitudRepo.findBySolicitanteId(2L)).thenReturn(List.of(solicitudEntity(EstadoSolicitud.APROBADA)));
        when(solicitudRepo.findByAnimalVendedorId(1L)).thenReturn(List.of(solicitudEntity(EstadoSolicitud.PENDIENTE)));
        when(solicitudRepo.findByAnimalIdAndEstado(10L, EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitudEntity(EstadoSolicitud.PENDIENTE)));
        when(solicitudRepo.existsBySolicitanteIdAndAnimalVendedorId(2L, 1L)).thenReturn(true);
        when(solicitudRepo.existsByAnimalIdAndSolicitanteIdAndEstadoIn(eq(10L), eq(2L), anyCollection())).thenReturn(true);

        assertThat(adapter.buscarPorId(5L)).map(Solicitud::mensaje).contains("Hola");
        assertThat(adapter.listarPorSolicitante(2L)).extracting(Solicitud::estado).containsExactly(EstadoSolicitud.APROBADA);
        assertThat(adapter.listarPorVendedor(1L)).hasSize(1);
        assertThat(adapter.listarPendientesPorAnimal(10L)).hasSize(1);
        assertThat(adapter.existeSolicitudDeSolicitanteAVendedor(2L, 1L)).isTrue();
        assertThat(adapter.existeActivaPorAnimalYSolicitante(10L, 2L)).isTrue();
    }

    // ---------- Tokens de recuperación ----------

    @Test
    void tokens_de_recuperacion() {
        TokenRecuperacionJpaRepository tokenRepo = mock(TokenRecuperacionJpaRepository.class);
        TokenRecuperacionPersistenceAdapter adapter = new TokenRecuperacionPersistenceAdapter(tokenRepo, usuarioRepo);
        String hash = "a".repeat(64);
        when(usuarioRepo.getReferenceById(2L)).thenReturn(adoptanteEntity);
        when(tokenRepo.save(any(TokenRecuperacionEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepo.findByTokenHash(hash)).thenReturn(Optional.of(
                new TokenRecuperacionEntity(1L, adoptanteEntity, hash, fecha, fecha.plusMinutes(30), false)));
        when(tokenRepo.anularVigentesDeUsuario(2L)).thenReturn(2);
        when(tokenRepo.eliminarVencidos(fecha)).thenReturn(3);

        TokenRecuperacion guardado = adapter.guardar(
                new TokenRecuperacion(null, 2L, hash, fecha, fecha.plusMinutes(30), false));

        assertThat(guardado.usuarioId()).isEqualTo(2L);
        assertThat(adapter.buscarPorHash(hash)).map(TokenRecuperacion::usado).contains(false);
        assertThat(adapter.anularVigentesDeUsuario(2L)).isEqualTo(2);
        assertThat(adapter.eliminarVencidos(fecha)).isEqualTo(3);
    }

    // ---------- Historial ----------

    @Test
    void historial_de_solicitudes() {
        HistorialSolicitudJpaRepository historialRepo = mock(HistorialSolicitudJpaRepository.class);
        HistorialSolicitudPersistenceAdapter adapter =
                new HistorialSolicitudPersistenceAdapter(historialRepo, solicitudRepo, usuarioRepo);
        SolicitudEntity solicitud = solicitudEntity(EstadoSolicitud.APROBADA);
        when(solicitudRepo.getReferenceById(5L)).thenReturn(solicitud);
        when(usuarioRepo.getReferenceById(1L)).thenReturn(vendedorEntity);
        when(historialRepo.save(any(HistorialSolicitudEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(historialRepo.findBySolicitudIdOrderByFechaAscIdAsc(5L)).thenReturn(List.of(
                new HistorialSolicitudEntity(1L, solicitud, null, EstadoSolicitud.PENDIENTE, adoptanteEntity, fecha)));

        HistorialSolicitud registrado = adapter.registrar(
                HistorialSolicitud.de(5L, EstadoSolicitud.PENDIENTE, EstadoSolicitud.APROBADA, 1L, fecha));

        ArgumentCaptor<HistorialSolicitudEntity> c = ArgumentCaptor.forClass(HistorialSolicitudEntity.class);
        verify(historialRepo).save(c.capture());
        assertThat(c.getValue().getEstadoNuevo()).isEqualTo(EstadoSolicitud.APROBADA);
        assertThat(registrado.usuarioId()).isEqualTo(1L);
        assertThat(adapter.listarPorSolicitud(5L)).extracting(HistorialSolicitud::estadoAnterior).containsExactly((EstadoSolicitud) null);
    }
}
