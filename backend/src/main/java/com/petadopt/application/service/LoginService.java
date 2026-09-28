package com.petadopt.application.service;

import com.petadopt.application.port.in.LoginUseCase;
import com.petadopt.application.port.out.PasswordHasherPort;
import com.petadopt.application.port.out.TokenServicePort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.CredencialesInvalidasException;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenServicePort tokenService;

    public LoginService(UsuarioRepositoryPort usuarioRepository,
                        PasswordHasherPort passwordHasher,
                        TokenServicePort tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    @Override
    public Resultado login(Comando comando) {
        String email = comando.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordHasher.coincide(comando.password(), usuario.password())) {
            throw new CredencialesInvalidasException();
        }

        return new Resultado(tokenService.generar(usuario), usuario);
    }
}