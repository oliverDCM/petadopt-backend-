package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.application.port.out.HistorialSolicitudRepositoryPort;
import com.petadopt.domain.model.HistorialSolicitud;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HistorialSolicitudPersistenceAdapter implements HistorialSolicitudRepositoryPort {

    private final HistorialSolicitudJpaRepository jpaRepository;
    private final SolicitudJpaRepository solicitudJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;

    public HistorialSolicitudPersistenceAdapter(HistorialSolicitudJpaRepository jpaRepository,
                                                SolicitudJpaRepository solicitudJpaRepository,
                                                UsuarioJpaRepository usuarioJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.solicitudJpaRepository = solicitudJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public HistorialSolicitud registrar(HistorialSolicitud h) {
        HistorialSolicitudEntity entidad = new HistorialSolicitudEntity(h.id(),
                solicitudJpaRepository.getReferenceById(h.solicitudId()),
                h.estadoAnterior(), h.estadoNuevo(),
                usuarioJpaRepository.getReferenceById(h.usuarioId()), h.fecha());
        return toDomain(jpaRepository.save(entidad));
    }

    @Override
    public List<HistorialSolicitud> listarPorSolicitud(Long solicitudId) {
        return jpaRepository.findBySolicitudIdOrderByFechaAscIdAsc(solicitudId).stream()
                .map(this::toDomain)
                .toList();
    }

    private HistorialSolicitud toDomain(HistorialSolicitudEntity e) {
        return new HistorialSolicitud(e.getId(), e.getSolicitud().getId(), e.getEstadoAnterior(),
                e.getEstadoNuevo(), e.getUsuario().getId(), e.getFecha());
    }
}
