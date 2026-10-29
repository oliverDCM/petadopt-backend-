package com.petadopt.application.service;

import com.petadopt.application.port.in.RestablecerContrasenaUseCase;
import com.petadopt.application.port.out.EmailSenderPort;
import com.petadopt.application.port.out.PasswordHasherPort;
import com.petadopt.application.port.out.TokenRecuperacionRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.ContrasenaInvalidaException;
import com.petadopt.domain.exception.TokenRecuperacionInvalidoException;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.TokenRecuperacion;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestablecerContrasenaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 28, 15, 0);
    private static final String TOKEN = "Jx3b9tq0Yc2n8w4dA1fR7mZpLkQe5sVhUiOyTrEwBnM";

    @Mock
    private TokenRecuperacionRepositoryPort tokenRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private EmailSenderPort emailSender;

    private RestablecerContrasenaService service;

    private final Usuario ana = new Usuario(7L, "Ana", "ana@test.com", "hashViejo", "300", "Bogotá", Rol.ADOPTANTE);

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(ZonedDateTime.of(AHORA, ZONA).toInstant(), ZONA);
        service = new RestablecerContrasenaService(tokenRepository, usuarioRepository, passwordHasher,
                emailSender, reloj);
    }

    private TokenRecuperacion token(LocalDateTime expira, boolean usado) {
        return new TokenRecuperacion(1L, 7L, TokenSeguro.hash(TOKEN), AHORA.minusMinutes(10), expira, usado);
    }

    private RestablecerContrasenaUseCase.Comando comando(String password) {
        return new RestablecerContrasenaUseCase.Comando(TOKEN, password);
    }

    @Test
    void cambio_exitoso_cifra_la_clave_marca_el_token_y_avisa_por_correo() {
        when(tokenRepository.buscarPorHash(TokenSeguro.hash(TOKEN))).thenReturn(Optional.of(token(AHORA.plusMinutes(20), false)));
        when(usuarioRepository.buscarPorId(7L)).thenReturn(Optional.of(ana));
        when(passwordHasher.cifrar("nuevaClave123")).thenReturn("hashNuevo");

        service.restablecer(comando("nuevaClave123"));

        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(usuario.capture());
        assertThat(usuario.getValue().password()).isEqualTo("hashNuevo");
        assertThat(usuario.getValue().email()).isEqualTo("ana@test.com");

        ArgumentCaptor<TokenRecuperacion> tokenGuardado = ArgumentCaptor.forClass(TokenRecuperacion.class);
        verify(tokenRepository).guardar(tokenGuardado.capture());
        assertThat(tokenGuardado.getValue().usado()).isTrue();
        verify(tokenRepository).anularVigentesDeUsuario(7L);
        verify(emailSender).enviar(eq("ana@test.com"), anyString(), anyString());
    }

    @Test
    void token_reutilizado_devuelve_400() {
        when(tokenRepository.buscarPorHash(TokenSeguro.hash(TOKEN))).thenReturn(Optional.of(token(AHORA.plusMinutes(20), true)));

        TokenRecuperacionInvalidoException ex = assertThrows(TokenRecuperacionInvalidoException.class,
                () -> service.restablecer(comando("nuevaClave123")));
        assertThat(ex.getMessage()).contains("ya fue usado");
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void token_vencido_devuelve_400() {
        when(tokenRepository.buscarPorHash(TokenSeguro.hash(TOKEN))).thenReturn(Optional.of(token(AHORA.minusMinutes(1), false)));

        TokenRecuperacionInvalidoException ex = assertThrows(TokenRecuperacionInvalidoException.class,
                () -> service.restablecer(comando("nuevaClave123")));
        assertThat(ex.getMessage()).contains("venció");
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void token_inexistente_devuelve_400() {
        when(tokenRepository.buscarPorHash(anyString())).thenReturn(Optional.empty());

        assertThrows(TokenRecuperacionInvalidoException.class, () -> service.restablecer(comando("nuevaClave123")));
        verifyNoInteractions(usuarioRepository, passwordHasher, emailSender);
    }

    @Test
    void contrasena_demasiado_corta_devuelve_400() {
        assertThrows(ContrasenaInvalidaException.class, () -> service.restablecer(comando("1234567")));
        verifyNoInteractions(tokenRepository, usuarioRepository);
    }

    @Test
    void contrasena_demasiado_larga_devuelve_400() {
        assertThrows(ContrasenaInvalidaException.class, () -> service.restablecer(comando("a".repeat(73))));
        verifyNoInteractions(tokenRepository, usuarioRepository);
    }

    @Test
    void token_vacio_devuelve_400() {
        assertThrows(TokenRecuperacionInvalidoException.class,
                () -> service.restablecer(new RestablecerContrasenaUseCase.Comando("  ", "nuevaClave123")));
        verifyNoInteractions(tokenRepository);
    }
}
