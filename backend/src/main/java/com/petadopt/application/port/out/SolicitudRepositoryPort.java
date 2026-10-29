package com.petadopt.application.port.out;

import com.petadopt.domain.model.Solicitud;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepositoryPort {
    boolean existeActivaPorAnimalYSolicitante(Long animalId, Long solicitanteId);

    Solicitud guardar(Solicitud solicitud);

    Optional<Solicitud> buscarPorId(Long id);

    List<Solicitud> listarPorSolicitante(Long solicitanteId);

    List<Solicitud> listarPorVendedor(Long vendedorId);

    List<Solicitud> listarPendientesPorAnimal(Long animalId);

    boolean existeSolicitudDeSolicitanteAVendedor(Long solicitanteId, Long vendedorId);

}