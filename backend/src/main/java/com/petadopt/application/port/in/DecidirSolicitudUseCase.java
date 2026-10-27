package com.petadopt.application.port.in;

import com.petadopt.domain.model.Solicitud;

public interface DecidirSolicitudUseCase {

    Solicitud decidir(Comando comando);

    enum Decision {
        APROBAR,
        RECHAZAR
    }

    record Comando(String vendedorEmail, Long solicitudId, Decision decision) {
    }
}