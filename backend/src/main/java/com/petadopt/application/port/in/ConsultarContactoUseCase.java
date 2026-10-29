package com.petadopt.application.port.in;

import com.petadopt.domain.model.Contacto;

public interface ConsultarContactoUseCase {

    Contacto consultar(String emailUsuario, Long solicitudId);
}
