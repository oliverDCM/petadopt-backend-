package com.petadopt.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.petadopt.domain.model.EstadoSolicitud;
import java.util.Collection;

import java.util.List;
import java.util.Optional;

public interface SolicitudJpaRepository extends JpaRepository<SolicitudEntity, Long> {

    List<SolicitudEntity> findBySolicitanteId(Long solicitanteId);

    boolean existsBySolicitanteIdAndAnimalVendedorId(Long solicitanteId, Long vendedorId);

    List<SolicitudEntity> findByAnimalVendedorId(Long vendedorId);

    List<SolicitudEntity> findByAnimalIdAndEstado(Long animalId, EstadoSolicitud estado);

    List<SolicitudEntity> findByAnimalIdAndEstadoIn(Long animalId, Collection<EstadoSolicitud> estados);

    /** Proyección: no carga la entidad, así una lectura posterior trae el estado actual de la base de datos. */
    @Query("select s.animal.id from SolicitudEntity s where s.id = :id")
    Optional<Long> buscarAnimalId(@Param("id") Long id);

    boolean existsByAnimalIdAndSolicitanteIdAndEstadoIn(Long animalId, Long solicitanteId,
                                                        Collection<EstadoSolicitud> estados);
}