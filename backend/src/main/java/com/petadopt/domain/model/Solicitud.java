package com.petadopt.domain.model;

import java.time.LocalDateTime;

public record Solicitud(
        Long id,
        Long animalId,
        Long solicitanteId,
        String mensaje,
        EstadoSolicitud estado,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaDecision
) {
    public Solicitud conDecision(EstadoSolicitud nuevoEstado, LocalDateTime fecha) {
    return new Solicitud(id, animalId, solicitanteId, mensaje, nuevoEstado, fechaSolicitud, fecha);
}
}