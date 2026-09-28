package com.petadopt.application.port.out;

import com.petadopt.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorEmail(String email);
}