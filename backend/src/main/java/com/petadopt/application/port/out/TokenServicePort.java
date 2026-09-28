package com.petadopt.application.port.out;

import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;

import java.util.Optional;

public interface TokenServicePort {

    String generar(Usuario usuario);

    Optional<DatosToken> validar(String token);

    record DatosToken(String email, Rol rol) {
    }
}