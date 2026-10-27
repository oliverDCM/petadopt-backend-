package com.petadopt.application.port.in;

import com.petadopt.domain.model.Solicitud;

public interface EnviarSolicitudUseCase {

    Solicitud enviar(Comando comando);

    record Comando(String solicitanteEmail, Long animalId, String mensaje) {
    }
}