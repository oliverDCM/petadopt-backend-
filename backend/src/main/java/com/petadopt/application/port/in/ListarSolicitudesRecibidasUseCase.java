package com.petadopt.application.port.in;

import com.petadopt.domain.model.Solicitud;

import java.util.List;

public interface ListarSolicitudesRecibidasUseCase {

    List<Solicitud> listar(String vendedorEmail);
}