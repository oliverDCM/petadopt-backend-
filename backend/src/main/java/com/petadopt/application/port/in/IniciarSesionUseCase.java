package com.petadopt.application.port.in;

import com.petadopt.domain.model.Rol;

public interface IniciarSesionUseCase {

    Resultado iniciarSesion(Comando comando);

    record Comando(String email, String password) {
    }

    record Resultado(String token, Rol rol, String nombre, String email) {
    }
}
