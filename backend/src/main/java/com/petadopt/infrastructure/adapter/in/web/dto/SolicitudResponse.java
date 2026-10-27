package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.Solicitud;

import java.time.LocalDateTime;

public record SolicitudResponse(
        Long id,
        Long animalId,
        Long solicitanteId,
        String mensaje,
        EstadoSolicitud estado,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaDecision
) {
    public static SolicitudResponse desde(Solicitud s) {
        return new SolicitudResponse(s.id(), s.animalId(), s.solicitanteId(), s.mensaje(),
                s.estado(), s.fechaSolicitud(), s.fechaDecision());
    }
}