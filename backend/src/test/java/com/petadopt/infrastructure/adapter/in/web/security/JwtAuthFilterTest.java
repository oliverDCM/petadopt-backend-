package com.petadopt.infrastructure.adapter.in.web.security;

import com.petadopt.application.port.out.TokenServicePort;
import com.petadopt.application.port.out.TokenServicePort.DatosToken;
import com.petadopt.domain.model.Rol;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    private TokenServicePort tokens;
    private JwtAuthFilter filtro;

    @BeforeEach
    void preparar() {
        SecurityContextHolder.clearContext();
        tokens = mock(TokenServicePort.class);
        filtro = new JwtAuthFilter(tokens);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private MockFilterChain ejecutar(String cabeceraAuthorization) throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/animales");
        if (cabeceraAuthorization != null) {
            request.addHeader("Authorization", cabeceraAuthorization);
        }
        var chain = new MockFilterChain();
        filtro.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }

    @Test
    void conTokenValidoAutenticaAlUsuarioConSuRol() throws Exception {
        when(tokens.validar("abc")).thenReturn(Optional.of(new DatosToken(7L, "ana@correo.com", Rol.VENDEDOR)));

        var chain = ejecutar("Bearer abc");

        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertThat(autenticacion).isNotNull();
        assertThat(autenticacion.getPrincipal()).isEqualTo(new UsuarioAutenticado(7L, "ana@correo.com", Rol.VENDEDOR));
        assertThat(autenticacion.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_VENDEDOR");
        assertThat(chain.getRequest()).isNotNull(); // la petición siguió su camino
    }

    @Test
    void sinCabeceraNoAutentica() throws Exception {
        ejecutar(null);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void conTokenInvalidoNoAutentica() throws Exception {
        when(tokens.validar("malo")).thenReturn(Optional.empty());

        ejecutar("Bearer malo");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void ignoraCabecerasQueNoSonBearer() throws Exception {
        ejecutar("Basic dXNlcjpwYXNz");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
