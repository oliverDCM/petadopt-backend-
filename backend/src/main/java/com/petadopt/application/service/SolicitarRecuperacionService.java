package com.petadopt.application.service;

import com.petadopt.application.port.in.SolicitarRecuperacionUseCase;
import com.petadopt.application.port.out.EmailSenderPort;
import com.petadopt.application.port.out.LimitadorIntentosPort;
import com.petadopt.application.port.out.TokenRecuperacionRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.LimiteSolicitudesExcedidoException;
import com.petadopt.domain.model.TokenRecuperacion;
import com.petadopt.domain.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class SolicitarRecuperacionService implements SolicitarRecuperacionUseCase {

    static final long MINUTOS_VIGENCIA = 30;

    private final UsuarioRepositoryPort usuarioRepository;
    private final TokenRecuperacionRepositoryPort tokenRepository;
    private final EmailSenderPort emailSender;
    private final LimitadorIntentosPort limitador;
    private final Clock clock;
    private final String frontendUrl;

    public SolicitarRecuperacionService(UsuarioRepositoryPort usuarioRepository,
                                        TokenRecuperacionRepositoryPort tokenRepository,
                                        EmailSenderPort emailSender,
                                        LimitadorIntentosPort limitador,
                                        Clock clock,
                                        @Value("${app.frontend-url}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.emailSender = emailSender;
        this.limitador = limitador;
        this.clock = clock;
        this.frontendUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
    }

    @Override
    @Transactional
    public void solicitar(Comando comando) {
        String email = comando.email().trim().toLowerCase(Locale.ROOT);

        // El límite se aplica exista o no la cuenta, para no revelar qué emails están registrados.
        if (!limitador.intentar("email:" + email) || !limitador.intentar("ip:" + comando.ip())) {
            throw new LimiteSolicitudesExcedidoException();
        }

        usuarioRepository.buscarPorEmail(email).ifPresent(this::generarYEnviar);
    }

    private void generarYEnviar(Usuario usuario) {
        tokenRepository.anularVigentesDeUsuario(usuario.id());

        String token = TokenSeguro.generar();
        LocalDateTime ahora = LocalDateTime.now(clock);
        tokenRepository.guardar(new TokenRecuperacion(null, usuario.id(), TokenSeguro.hash(token),
                ahora, ahora.plusMinutes(MINUTOS_VIGENCIA), false));

        String enlace = frontendUrl + "/restablecer-contrasena?token=" + token;
        PlantillasCorreo.Correo correo =
                PlantillasCorreo.recuperacionContrasena(usuario.nombre(), enlace, MINUTOS_VIGENCIA);
        emailSender.enviar(usuario.email(), correo.asunto(), correo.cuerpo());
    }
}
