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
}