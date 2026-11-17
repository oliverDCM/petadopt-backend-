package com.petadopt.application.service;

import com.petadopt.application.port.in.IniciarSesionUseCase;
import com.petadopt.application.port.out.PasswordHasherPort;
import com.petadopt.application.port.out.ProteccionFuerzaBrutaPort;
import com.petadopt.application.port.out.TokenServicePort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.CredencialesInvalidasException;
import com.petadopt.domain.exception.DemasiadosIntentosException;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IniciarSesionServiceTest {

    private UsuarioRepositoryPort repositorio;
    private PasswordHasherPort hasher;
    private TokenServicePort tokens;
    private ProteccionFuerzaBrutaPort proteccion;
    private IniciarSesionService servicio;

    private final Usuario ana = new Usuario(7L, "Ana", "ana@correo.com", "$2a$hash",
            "3001234567", "Bogotá", Rol.VENDEDOR);

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepositoryPort.class);
        hasher = mock(PasswordHasherPort.class);
        tokens = mock(TokenServicePort.class);
        when(hasher.cifrar(any())).thenReturn("$2a$relleno");
        proteccion = mock(ProteccionFuerzaBrutaPort.class);
        servicio = new IniciarSesionService(repositorio, hasher, tokens, proteccion);
    }

    @Test
    void conCredencialesCorrectasDevuelveTokenYDatosDeSesion() {
        when(repositorio.buscarPorEmail("ana@correo.com")).thenReturn(Optional.of(ana));
        when(hasher.coincide("clave1234", "$2a$hash")).thenReturn(true);
        when(tokens.generar(ana)).thenReturn("token-jwt");

        var resultado = servicio.iniciarSesion(new IniciarSesionUseCase.Comando("ana@correo.com", "clave1234"));

        assertThat(resultado.token()).isEqualTo("token-jwt");
        assertThat(resultado.rol()).isEqualTo(Rol.VENDEDOR);
        assertThat(resultado.nombre()).isEqualTo("Ana");
        assertThat(resultado.email()).isEqualTo("ana@correo.com");
    }

    @Test
    void normalizaElEmailAntesDeBuscar() {
        when(repositorio.buscarPorEmail("ana@correo.com")).thenReturn(Optional.of(ana));
        when(hasher.coincide("clave1234", "$2a$hash")).thenReturn(true);
        when(tokens.generar(ana)).thenReturn("token-jwt");

        var resultado = servicio.iniciarSesion(new IniciarSesionUseCase.Comando("  ANA@Correo.com ", "clave1234"));

        assertThat(resultado.token()).isEqualTo("token-jwt");
    }

    @Test
    void conContrasenaIncorrectaLanzaCredencialesInvalidas() {
        when(repositorio.buscarPorEmail("ana@correo.com")).thenReturn(Optional.of(ana));
        when(hasher.coincide("otra-clave", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> servicio.iniciarSesion(new IniciarSesionUseCase.Comando("ana@correo.com", "otra-clave")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void conEmailInexistenteLanzaLaMismaExcepcionYComparaContraElHashDeRelleno() {
        when(repositorio.buscarPorEmail("nadie@correo.com")).thenReturn(Optional.empty());
        when(hasher.coincide("clave1234", "$2a$relleno")).thenReturn(false);

        assertThatThrownBy(() -> servicio.iniciarSesion(new IniciarSesionUseCase.Comando("nadie@correo.com", "clave1234")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Email o contraseña incorrectos");
        // se hizo la comparación igualmente, para que el tiempo de respuesta no delate si el email existe
        verify(hasher).coincide("clave1234", "$2a$relleno");
    }

    @Test
    void cadaFalloSeRegistraPorIpYPorCuenta() {
        when(repositorio.buscarPorEmail("ana@correo.com")).thenReturn(Optional.of(ana));
        when(hasher.coincide("otra-clave", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> servicio.iniciarSesion(
                new IniciarSesionUseCase.Comando("ana@correo.com", "otra-clave", "203.0.113.9")))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(proteccion).registrarFallo("ip:203.0.113.9");
        verify(proteccion).registrarFallo("cuenta:ana@correo.com|203.0.113.9");
    }

    @Test
    void conDemasiadosFallosBloqueaSinConsultarLaCuentaNiCalcularElHash() {
        when(proteccion.superaLimite("cuenta:ana@correo.com|203.0.113.9", IniciarSesionService.MAX_FALLOS_POR_CUENTA))
                .thenReturn(true);

        assertThatThrownBy(() -> servicio.iniciarSesion(
                new IniciarSesionUseCase.Comando("ana@correo.com", "clave1234", "203.0.113.9")))
                .isInstanceOf(DemasiadosIntentosException.class);

        verify(repositorio, never()).buscarPorEmail(any());
        verify(hasher, never()).coincide(any(), any());
    }

    @Test
    void unaIpConDemasiadosFallosQuedaBloqueadaParaCualquierCuenta() {
        when(proteccion.superaLimite("ip:203.0.113.9", IniciarSesionService.MAX_FALLOS_POR_IP)).thenReturn(true);

        assertThatThrownBy(() -> servicio.iniciarSesion(
                new IniciarSesionUseCase.Comando("otra@correo.com", "clave1234", "203.0.113.9")))
                .isInstanceOf(DemasiadosIntentosException.class);
    }

    @Test
    void unInicioCorrectoBorraLosFallosDeEsaCuenta() {
        when(repositorio.buscarPorEmail("ana@correo.com")).thenReturn(Optional.of(ana));
        when(hasher.coincide("clave1234", "$2a$hash")).thenReturn(true);
        when(tokens.generar(ana)).thenReturn("token-jwt");

        servicio.iniciarSesion(new IniciarSesionUseCase.Comando("ana@correo.com", "clave1234", "203.0.113.9"));

        verify(proteccion).olvidar("cuenta:ana@correo.com|203.0.113.9");
        verify(proteccion, never()).registrarFallo(any());
    }
}
