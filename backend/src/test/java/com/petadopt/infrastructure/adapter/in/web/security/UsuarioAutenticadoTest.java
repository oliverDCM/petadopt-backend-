package com.petadopt.infrastructure.adapter.in.web.security;

import com.petadopt.domain.model.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioAutenticadoTest {

    @Test
    void authentication_getName_devuelve_el_email() {
        var principal = new UsuarioAutenticado(7L, "ana@correo.com", Rol.VENDEDOR);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of());

        assertThat(auth.getName()).isEqualTo("ana@correo.com");
    }
}
