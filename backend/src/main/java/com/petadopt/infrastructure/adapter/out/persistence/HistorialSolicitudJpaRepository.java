package com.petadopt.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialSolicitudJpaRepository extends JpaRepository<HistorialSolicitudEntity, Long> {

    List<HistorialSolicitudEntity> findBySolicitudIdOrderByFechaAscIdAsc(Long solicitudId);
}
