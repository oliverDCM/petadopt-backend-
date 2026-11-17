package com.petadopt.application.port.in;

import com.petadopt.domain.model.Rol;

public interface IniciarSesionUseCase {

    Resultado iniciarSesion(Comando comando);

    /** {@code ip}: dirección del cliente, para limitar los intentos fallidos. */
    record Comando(String email, String password, String ip) {

        public Comando(String email, String password) {
            this(email, password, "desconocida");
        }
    }

    record Resultado(String token, Rol rol, String nombre, String email) {
    }
}
