package com.petadopt.application.service;

import com.petadopt.application.port.in.RestablecerContrasenaUseCase;
import com.petadopt.application.port.out.EmailSenderPort;
import com.petadopt.application.port.out.PasswordHasherPort;
import com.petadopt.application.port.out.TokenRecuperacionRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.ContrasenaInvalidaException;
import com.petadopt.domain.exception.TokenRecuperacionInvalidoException;
import com.petadopt.domain.model.TokenRecuperacion;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class RestablecerContrasenaService implements RestablecerContrasenaUseCase {

    static final int LONGITUD_MINIMA = 8;
    static final int LONGITUD_MAXIMA = 72; // límite de BCrypt

    private final TokenRecuperacionRepositoryPort tokenRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordHasherPort passwordHasher;
    private final EmailSenderPort emailSender;
    private final Clock clock;

    public RestablecerContrasenaService(TokenRecuperacionRepositoryPort tokenRepository,
                                        UsuarioRepositoryPort usuarioRepository,
                                        PasswordHasherPort passwordHasher,
                                        EmailSenderPort emailSender,
                                        Clock clock) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.emailSender = emailSender;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void restablecer(Comando comando) {
        String nueva = comando.nuevaPassword();
        if (nueva == null || nueva.length() < LONGITUD_MINIMA || nueva.length() > LONGITUD_MAXIMA) {
            throw new ContrasenaInvalidaException();
        }
        if (comando.token() == null || comando.token().isBlank()) {
            throw new TokenRecuperacionInvalidoException("El enlace de recuperación no es válido");
        }

        TokenRecuperacion token = tokenRepository.buscarPorHash(TokenSeguro.hash(comando.token().trim()))
                .orElseThrow(() -> new TokenRecuperacionInvalidoException("El enlace de recuperación no es válido"));
        if (token.usado()) {
            throw new TokenRecuperacionInvalidoException(
                    "Este enlace de recuperación ya fue usado; solicita uno nuevo");
        }
        if (token.estaVencido(LocalDateTime.now(clock))) {
            throw new TokenRecuperacionInvalidoException(
                    "El enlace de recuperación venció; solicita uno nuevo");
        }

        Usuario usuario = usuarioRepository.buscarPorId(token.usuarioId())
                .orElseThrow(() -> new TokenRecuperacionInvalidoException("El enlace de recuperación no es válido"));

        usuarioRepository.guardar(usuario.conPassword(passwordHasher.cifrar(nueva)));
        tokenRepository.guardar(token.marcarUsado());
        tokenRepository.anularVigentesDeUsuario(usuario.id());

        PlantillasCorreo.Correo correo = PlantillasCorreo.contrasenaCambiada(usuario.nombre());
        emailSender.enviar(usuario.email(), correo.asunto(), correo.cuerpo());
    }
}
