package com.petadopt.application.service;

import com.petadopt.application.port.in.IniciarSesionUseCase;
import com.petadopt.application.port.out.PasswordHasherPort;
import com.petadopt.application.port.out.TokenServicePort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.CredencialesInvalidasException;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class IniciarSesionService implements IniciarSesionUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenServicePort tokenService;

    /**
     * Hash de relleno: cuando el email no existe se compara igualmente contra este hash,
     * para que la respuesta tarde lo mismo y no se pueda deducir qué emails están registrados.
     */
    private final String hashDeRelleno;

    public IniciarSesionService(UsuarioRepositoryPort usuarioRepository,
                                PasswordHasherPort passwordHasher,
                                TokenServicePort tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.hashDeRelleno = passwordHasher.cifrar("petadopt-hash-de-relleno");
    }

    @Override
    public Resultado iniciarSesion(Comando comando) {
        String email = comando.email().trim().toLowerCase();
        Usuario usuario = usuarioRepository.buscarPorEmail(email).orElse(null);

        String hashAComparar = usuario != null ? usuario.password() : hashDeRelleno;
        boolean coincide = passwordHasher.coincide(comando.password(), hashAComparar);

        if (usuario == null || !coincide) {
            throw new CredencialesInvalidasException();
        }

        String token = tokenService.generar(usuario);
        return new Resultado(token, usuario.rol(), usuario.nombre(), usuario.email());
    }
}
