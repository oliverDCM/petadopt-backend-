package com.petadopt.infrastructure.adapter.out.security;

import com.petadopt.application.port.out.TokenServicePort.DatosToken;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenAdapterTest {

    private static final String SECRETO = "un-secreto-de-prueba-con-mas-de-32-caracteres-123";
    private static final long UN_DIA_MS = 86_400_000L;

    private final Usuario ana = new Usuario(7L, "Ana", "ana@correo.com", "hash",
            null, null, Rol.ADOPTANTE);

    @Test
    void generaUnTokenQueSePuedeValidar() {
        var adaptador = new JwtTokenAdapter(SECRETO, UN_DIA_MS);

        String token = adaptador.generar(ana);
        Optional<DatosToken> datos = adaptador.validar(token);

        assertThat(datos).contains(
                new DatosToken(7L, "ana@correo.com", Rol.ADOPTANTE, adaptador.huellaCredencial(ana)));
    }

    @Test
    void rechazaUnTokenAlterado() {
        var adaptador = new JwtTokenAdapter(SECRETO, UN_DIA_MS);
        String token = adaptador.generar(ana);
        String alterado = token.substring(0, token.length() - 3) + "abc";

        assertThat(adaptador.validar(alterado)).isEmpty();
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        var otro = new JwtTokenAdapter("otro-secreto-distinto-con-mas-de-32-caracteres-9", UN_DIA_MS);
        var adaptador = new JwtTokenAdapter(SECRETO, UN_DIA_MS);

        assertThat(adaptador.validar(otro.generar(ana))).isEmpty();
    }

    @Test
    void rechazaUnTokenVencido() {
        var adaptador = new JwtTokenAdapter(SECRETO, -1_000L);

        assertThat(adaptador.validar(adaptador.generar(ana))).isEmpty();
    }

    @Test
    void rechazaTextoQueNoEsUnToken() {
        var adaptador = new JwtTokenAdapter(SECRETO, UN_DIA_MS);

        assertThat(adaptador.validar("esto-no-es-un-jwt")).isEmpty();
        assertThat(adaptador.validar("")).isEmpty();
    }

    @Test
    void laHuellaCambiaCuandoCambiaLaContrasena() {
        var adaptador = new JwtTokenAdapter(SECRETO, UN_DIA_MS);

        String antes = adaptador.huellaCredencial(ana);
        String despues = adaptador.huellaCredencial(ana.conPassword("otro-hash"));

        assertThat(antes).isEqualTo(adaptador.huellaCredencial(ana)); // estable para la misma contraseña
        assertThat(despues).isNotEqualTo(antes);
    }

    @Test
    void elTokenNoContieneElHashDeLaContrasena() {
        var adaptador = new JwtTokenAdapter(SECRETO, UN_DIA_MS);

        String token = adaptador.generar(ana);
        String cuerpo = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]),
                java.nio.charset.StandardCharsets.UTF_8);

        assertThat(cuerpo).doesNotContain("hash");
        assertThat(adaptador.huellaCredencial(ana)).hasSize(16);
    }

    @Test
    void noPermiteUnSecretoCorto() {
        assertThatThrownBy(() -> new JwtTokenAdapter("corto", UN_DIA_MS))
                .isInstanceOf(IllegalStateException.class);
    }
}
