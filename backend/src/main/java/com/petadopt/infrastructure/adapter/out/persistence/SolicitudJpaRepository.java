package com.petadopt.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import com.petadopt.domain.model.EstadoSolicitud;

import java.util.List;

public interface SolicitudJpaRepository extends JpaRepository<SolicitudEntity, Long> {

    List<SolicitudEntity> findBySolicitanteId(Long solicitanteId);

    List<SolicitudEntity> findByAnimalVendedorId(Long vendedorId);

    List<SolicitudEntity> findByAnimalIdAndEstado(Long animalId, EstadoSolicitud estado);
}