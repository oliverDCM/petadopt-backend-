package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.Solicitud;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class SolicitudPersistenceAdapter implements SolicitudRepositoryPort {

    private final SolicitudJpaRepository jpaRepository;
    private final AnimalJpaRepository animalJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;

    public SolicitudPersistenceAdapter(SolicitudJpaRepository jpaRepository,
                                       AnimalJpaRepository animalJpaRepository,
                                       UsuarioJpaRepository usuarioJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.animalJpaRepository = animalJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Solicitud guardar(Solicitud solicitud) {
        return toDomain(jpaRepository.save(toEntity(solicitud)));
    }

    @Override
    public Optional<Solicitud> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Solicitud> listarPorSolicitante(Long solicitanteId) {
        return jpaRepository.findBySolicitanteId(solicitanteId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Solicitud> listarPorVendedor(Long vendedorId) {
        return jpaRepository.findByAnimalVendedorId(vendedorId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Solicitud> listarPendientesPorAnimal(Long animalId) {
        return jpaRepository.findByAnimalIdAndEstado(animalId, EstadoSolicitud.PENDIENTE)
                .stream().map(this::toDomain).toList();
    }

    private SolicitudEntity toEntity(Solicitud s) {
        AnimalEntity animal = animalJpaRepository.getReferenceById(s.animalId());
        UsuarioEntity solicitante = usuarioJpaRepository.getReferenceById(s.solicitanteId());
        EstadoSolicitud estado = s.estado() != null ? s.estado() : EstadoSolicitud.PENDIENTE;
        LocalDateTime fecha = s.fechaSolicitud() != null ? s.fechaSolicitud() : LocalDateTime.now();
        return new SolicitudEntity(s.id(), animal, solicitante, s.mensaje(), estado, fecha, s.fechaDecision());
    }

    private Solicitud toDomain(SolicitudEntity e) {
        return new Solicitud(e.getId(), e.getAnimal().getId(), e.getSolicitante().getId(),
                e.getMensaje(), e.getEstado(), e.getFechaSolicitud(), e.getFechaDecision());
    }

    @Override
    public boolean existeSolicitudDeSolicitanteAVendedor(Long solicitanteId, Long vendedorId) {
        return jpaRepository.existsBySolicitanteIdAndAnimalVendedorId(solicitanteId, vendedorId);
    }

    @Override
    public boolean existeActivaPorAnimalYSolicitante(Long animalId, Long solicitanteId) {
        return jpaRepository.existsByAnimalIdAndSolicitanteIdAndEstadoIn(
                animalId, solicitanteId, List.of(EstadoSolicitud.PENDIENTE, EstadoSolicitud.APROBADA));
    }
}