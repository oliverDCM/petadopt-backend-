package com.petadopt.application.port.out;

import com.petadopt.domain.model.HistorialSolicitud;

import java.util.List;

public interface HistorialSolicitudRepositoryPort {

    HistorialSolicitud registrar(HistorialSolicitud cambio);

    List<HistorialSolicitud> listarPorSolicitud(Long solicitudId);
}
