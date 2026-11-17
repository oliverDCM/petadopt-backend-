package com.petadopt.application.service;

import com.petadopt.application.port.in.IniciarSesionUseCase;
import com.petadopt.application.port.out.PasswordHasherPort;
import com.petadopt.application.port.out.TokenServicePort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.application.port.out.ProteccionFuerzaBrutaPort;
import com.petadopt.domain.exception.CredencialesInvalidasException;
import com.petadopt.domain.exception.DemasiadosIntentosException;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class IniciarSesionService implements IniciarSesionUseCase {

    /** Fallos permitidos por cuenta desde una misma IP, y por IP en total, dentro de la ventana (15 min). */
    static final int MAX_FALLOS_POR_CUENTA = 5;
    // Más holgado que el de cuenta: detrás de una misma IP puede haber muchas personas (wifi de una universidad)
    static final int MAX_FALLOS_POR_IP = 100;

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenServicePort tokenService;
    private final ProteccionFuerzaBrutaPort proteccion;
    private final String hashDeRelleno;

    public IniciarSesionService(UsuarioRepositoryPort usuarioRepository,
                                PasswordHasherPort passwordHasher,
                                TokenServicePort tokenService,
                                ProteccionFuerzaBrutaPort proteccion) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.proteccion = proteccion;
        this.hashDeRelleno = passwordHasher.cifrar("petadopt-hash-de-relleno");
    }

    @Override
    public Resultado iniciarSesion(Comando comando) {
        String email = comando.email().trim().toLowerCase(Locale.ROOT);
        String claveIp = "ip:" + comando.ip();
        String claveCuenta = "cuenta:" + email + "|" + comando.ip();
        // Se comprueba antes de BCrypt: una IP bloqueada no consume CPU del servidor.
        if (proteccion.superaLimite(claveIp, MAX_FALLOS_POR_IP)
                || proteccion.superaLimite(claveCuenta, MAX_FALLOS_POR_CUENTA)) {
            throw new DemasiadosIntentosException();
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(email).orElse(null);
        // Si la cuenta no existe se compara contra un hash de relleno: el tiempo de respuesta no revela qué emails hay.
        String hashAComparar = usuario != null ? usuario.password() : hashDeRelleno;
        boolean coincide = passwordHasher.coincide(comando.password(), hashAComparar);
        if (usuario == null || !coincide) {
            proteccion.registrarFallo(claveIp);
            proteccion.registrarFallo(claveCuenta);
            throw new CredencialesInvalidasException();
        }
        proteccion.olvidar(claveCuenta);

        String token = tokenService.generar(usuario);
        return new Resultado(token, usuario.rol(), usuario.nombre(), usuario.email());
    }
}
