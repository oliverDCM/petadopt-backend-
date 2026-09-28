package com.petadopt.application.port.in;

import com.petadopt.domain.model.Usuario;

public interface LoginUseCase {

    Resultado login(Comando comando);

    record Comando(String email, String password) {
    }

    record Resultado(String token, Usuario usuario) {
    }
}