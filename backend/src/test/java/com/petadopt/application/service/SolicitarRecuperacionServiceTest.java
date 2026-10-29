package com.petadopt.application.service;

import com.petadopt.application.port.in.SolicitarRecuperacionUseCase;
import com.petadopt.application.port.out.EmailSenderPort;
import com.petadopt.application.port.out.LimitadorIntentosPort;
import com.petadopt.application.port.out.TokenRecuperacionRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.LimiteSolicitudesExcedidoException;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.TokenRecuperacion;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitarRecuperacionServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 28, 15, 0);

    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private TokenRecuperacionRepositoryPort tokenRepository;
    @Mock
    private EmailSenderPort emailSender;
    @Mock
    private LimitadorIntentosPort limitador;

    private SolicitarRecuperacionService service;

    private final Usuario ana = new Usuario(7L, "Ana", "ana@test.com", "hash", "300", "Bogotá", Rol.ADOPTANTE);

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(ZonedDateTime.of(AHORA, ZONA).toInstant(), ZONA);
        service = new SolicitarRecuperacionService(usuarioRepository, tokenRepository, emailSender,
                limitador, reloj, "http://localhost:4200/");
    }

    private void permitirIntentos() {
        when(limitador.intentar(anyString())).thenReturn(true);
    }

    @Test
    void email_registrado_genera_token_de_30_minutos_y_envia_el_enlace() {
        permitirIntentos();
        when(usuarioRepository.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(ana));

        service.solicitar(new SolicitarRecuperacionUseCase.Comando("  ANA@test.com ", "10.0.0.1"));

        ArgumentCaptor<TokenRecuperacion> tokenCaptor = ArgumentCaptor.forClass(TokenRecuperacion.class);
        verify(tokenRepository).guardar(tokenCaptor.capture());
        TokenRecuperacion guardado = tokenCaptor.getValue();
        assertThat(guardado.usuarioId()).isEqualTo(7L);
        assertThat(guardado.usado()).isFalse();
        assertThat(guardado.fechaCreacion()).isEqualTo(AHORA);
        assertThat(guardado.fechaExpiracion()).isEqualTo(AHORA.plusMinutes(30));
        assertThat(guardado.tokenHash()).hasSize(64).matches("[0-9a-f]{64}");

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(emailSender).enviar(eq("ana@test.com"), anyString(), cuerpo.capture());
        Matcher m = Pattern.compile("http://localhost:4200/restablecer-contrasena\\?token=([A-Za-z0-9_-]+)")
                .matcher(cuerpo.getValue());
        assertThat(m.find()).isTrue();
        String tokenDelCorreo = m.group(1);
        assertThat(tokenDelCorreo).hasSize(43); // 32 bytes en Base64 URL sin relleno
        // En la base solo queda el hash, nunca el token
        assertThat(guardado.tokenHash()).isEqualTo(TokenSeguro.hash(tokenDelCorreo)).isNotEqualTo(tokenDelCorreo);
    }

    @Test
    void anula_los_tokens_anteriores_antes_de_crear_el_nuevo() {
        permitirIntentos();
        when(usuarioRepository.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(ana));

        service.solicitar(new SolicitarRecuperacionUseCase.Comando("ana@test.com", "10.0.0.1"));

        InOrder orden = inOrder(tokenRepository);
        orden.verify(tokenRepository).anularVigentesDeUsuario(7L);
        orden.verify(tokenRepository).guardar(any(TokenRecuperacion.class));
    }

    @Test
    void email_inexistente_termina_igual_sin_crear_token_ni_enviar_correo() {
        permitirIntentos();
        when(usuarioRepository.buscarPorEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.solicitar(new SolicitarRecuperacionUseCase.Comando("nadie@test.com", "10.0.0.1")));

        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void supera_el_limite_por_email_devuelve_429() {
        when(limitador.intentar("email:ana@test.com")).thenReturn(false);

        assertThrows(LimiteSolicitudesExcedidoException.class,
                () -> service.solicitar(new SolicitarRecuperacionUseCase.Comando("ana@test.com", "10.0.0.1")));
        verify(usuarioRepository, never()).buscarPorEmail(anyString());
        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void supera_el_limite_por_ip_devuelve_429() {
        when(limitador.intentar("email:ana@test.com")).thenReturn(true);
        when(limitador.intentar("ip:10.0.0.1")).thenReturn(false);

        assertThrows(LimiteSolicitudesExcedidoException.class,
                () -> service.solicitar(new SolicitarRecuperacionUseCase.Comando("ana@test.com", "10.0.0.1")));
        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void el_limite_tambien_aplica_a_emails_no_registrados() {
        when(limitador.intentar("email:nadie@test.com")).thenReturn(false);

        assertThrows(LimiteSolicitudesExcedidoException.class,
                () -> service.solicitar(new SolicitarRecuperacionUseCase.Comando("nadie@test.com", "10.0.0.1")));
    }
}
