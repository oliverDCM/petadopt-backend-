package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.ConsultarPerfilPublicoUseCase;
import com.petadopt.application.port.in.IniciarSesionUseCase;
import com.petadopt.application.port.in.RegistrarUsuarioUseCase;
import com.petadopt.application.port.in.RestablecerContrasenaUseCase;
import com.petadopt.application.port.in.SolicitarRecuperacionUseCase;
import com.petadopt.domain.model.PerfilPublico;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import com.petadopt.infrastructure.adapter.in.web.dto.LoginRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.LoginResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.MensajeResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.OlvideContrasenaRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.PerfilPublicoResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.RegistroRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.RestablecerContrasenaRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.UsuarioResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthYUsuarioControllerTest {

    private final RegistrarUsuarioUseCase registrar = mock(RegistrarUsuarioUseCase.class);
    private final IniciarSesionUseCase iniciarSesion = mock(IniciarSesionUseCase.class);
    private final SolicitarRecuperacionUseCase solicitarRecuperacion = mock(SolicitarRecuperacionUseCase.class);
    private final RestablecerContrasenaUseCase restablecer = mock(RestablecerContrasenaUseCase.class);

    private final AuthController auth = new AuthController(registrar, iniciarSesion, solicitarRecuperacion, restablecer);

    @Test
    void registrar_responde_201_sin_la_contrasena() {
        when(registrar.registrar(new RegistrarUsuarioUseCase.Comando("Ana", "ana@test.com", "12345678", "300", "Bogotá", Rol.ADOPTANTE)))
                .thenReturn(new Usuario(2L, "Ana", "ana@test.com", "hash", "300", "Bogotá", Rol.ADOPTANTE));

        ResponseEntity<UsuarioResponse> r = auth.registrar(
                new RegistroRequest("Ana", "ana@test.com", "12345678", "300", "Bogotá", Rol.ADOPTANTE));

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(r.getBody().id()).isEqualTo(2L);
        assertThat(r.getBody().toString()).doesNotContain("hash");
    }

    @Test
    void login_devuelve_token_rol_nombre_y_email() {
        when(iniciarSesion.iniciarSesion(new IniciarSesionUseCase.Comando("ana@test.com", "12345678")))
                .thenReturn(new IniciarSesionUseCase.Resultado("jwt", Rol.ADOPTANTE, "Ana", "ana@test.com"));

        LoginResponse r = auth.login(new LoginRequest("ana@test.com", "12345678"));

        assertThat(r.token()).isEqualTo("jwt");
        assertThat(r.rol()).isEqualTo(Rol.ADOPTANTE);
    }

    @Test
    void olvide_contrasena_usa_la_ip_del_cliente_y_responde_generico() {
        MockHttpServletRequest http = new MockHttpServletRequest();
        http.setRemoteAddr("10.0.0.7");

        MensajeResponse r = auth.olvideContrasena(new OlvideContrasenaRequest("ana@test.com"), http);

        verify(solicitarRecuperacion).solicitar(new SolicitarRecuperacionUseCase.Comando("ana@test.com", "10.0.0.7"));
        assertThat(r.mensaje()).startsWith("Si el correo está registrado");
    }

    @Test
    void restablecer_contrasena_llama_al_caso_de_uso() {
        MensajeResponse r = auth.restablecerContrasena(new RestablecerContrasenaRequest("tok", "nuevaClave1"));

        verify(restablecer).restablecer(new RestablecerContrasenaUseCase.Comando("tok", "nuevaClave1"));
        assertThat(r.mensaje()).contains("contraseña se actualizó");
    }

    @Test
    void perfil_publico_devuelve_solo_nombre_ciudad_y_rol() {
        ConsultarPerfilPublicoUseCase consultar = mock(ConsultarPerfilPublicoUseCase.class);
        when(consultar.consultar("v@test.com", 2L)).thenReturn(new PerfilPublico("Ana", "Bogotá", Rol.ADOPTANTE));

        PerfilPublicoResponse r = new UsuarioController(consultar)
                .perfilPublico(2L, new UsernamePasswordAuthenticationToken("v@test.com", null));

        assertThat(r).isEqualTo(new PerfilPublicoResponse("Ana", "Bogotá", Rol.ADOPTANTE));
    }

    @Test
    void health_responde_up() {
        Map<String, String> r = new HealthController().health();
        assertThat(r).containsEntry("status", "UP");
    }
}
