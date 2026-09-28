package com.petadopt.application.service;

import com.petadopt.application.port.in.RegistrarUsuarioUseCase;
import com.petadopt.application.port.out.PasswordHasherPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.EmailYaRegistradoException;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordHasherPort passwordHasher;

    public RegistrarUsuarioService(UsuarioRepositoryPort usuarioRepository,
                                   PasswordHasherPort passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Usuario registrar(Comando comando) {
        String email = comando.email().trim().toLowerCase();

        if (usuarioRepository.existePorEmail(email)) {
            throw new EmailYaRegistradoException(email);
        }

        Usuario nuevo = new Usuario(
                null,
                comando.nombre().trim(),
                email,
                passwordHasher.cifrar(comando.password()),
                comando.telefono(),
                comando.ciudad(),
                comando.rol()
        );
        return usuarioRepository.guardar(nuevo);
    }
}