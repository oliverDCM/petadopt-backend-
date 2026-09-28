package com.petadopt.application.port.in;

import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;

public interface RegistrarUsuarioUseCase {

    Usuario registrar(Comando comando);

    record Comando(String nombre, String email, String password,
                   String telefono, String ciudad, Rol rol) {
    }
}