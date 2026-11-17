package com.petadopt.application.port.out;

import com.petadopt.domain.model.Solicitud;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepositoryPort {
    boolean existeActivaPorAnimalYSolicitante(Long animalId, Long solicitanteId);

    Solicitud guardar(Solicitud solicitud);

    Optional<Solicitud> buscarPorId(Long id);

    /** Id del animal de una solicitud, sin cargar la solicitud (para bloquear primero el animal). */
    Optional<Long> buscarAnimalId(Long solicitudId);

    /** Solicitudes PENDIENTES o APROBADAS de un animal. */
    List<Solicitud> listarActivasPorAnimal(Long animalId);

    List<Solicitud> listarPorSolicitante(Long solicitanteId);

    List<Solicitud> listarPorVendedor(Long vendedorId);

    List<Solicitud> listarPendientesPorAnimal(Long animalId);

    boolean existeSolicitudDeSolicitanteAVendedor(Long solicitanteId, Long vendedorId);

}