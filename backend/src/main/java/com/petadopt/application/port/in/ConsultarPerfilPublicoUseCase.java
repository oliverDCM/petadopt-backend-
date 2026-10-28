package com.petadopt.application.port.in;

import com.petadopt.domain.model.PerfilPublico;

public interface ConsultarPerfilPublicoUseCase {

    PerfilPublico consultar(String vendedorEmail, Long usuarioId);
}