package com.petadopt.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Formato único de error de la API (HU-BE-17).
 * "error" repite el mensaje para que el frontend que ya lee ese campo siga funcionando.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int codigo,
        String mensaje,
        String error,
        OffsetDateTime timestamp,
        String ruta,
        Map<String, String> campos
) {
    public static ErrorResponse de(int codigo, String mensaje, String ruta) {
        return new ErrorResponse(codigo, mensaje, mensaje, OffsetDateTime.now(), ruta, null);
    }

    public static ErrorResponse deValidacion(String mensaje, String ruta, Map<String, String> campos) {
        return new ErrorResponse(400, mensaje, mensaje, OffsetDateTime.now(), ruta, campos);
    }
}
