package com.petadopt.domain.model;

import java.time.LocalDateTime;

/** Registro de un cambio de estado de una solicitud: quién lo hizo y cuándo. */
public record HistorialSolicitud(
        Long id,
        Long solicitudId,
        EstadoSolicitud estadoAnterior,
        EstadoSolicitud estadoNuevo,
        Long usuarioId,
        LocalDateTime fecha
) {
    public static HistorialSolicitud de(Long solicitudId, EstadoSolicitud anterior, EstadoSolicitud nuevo,
                                        Long usuarioId, LocalDateTime fecha) {
        return new HistorialSolicitud(null, solicitudId, anterior, nuevo, usuarioId, fecha);
    }
}
