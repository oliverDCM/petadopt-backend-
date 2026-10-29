package com.petadopt.infrastructure.adapter.in.web.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestablecerContrasenaRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciar() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrar() {
        factory.close();
    }

    @Test
    void acepta_contrasena_de_8_a_72_caracteres() {
        assertThat(validator.validate(new RestablecerContrasenaRequest("tok", "12345678"))).isEmpty();
        assertThat(validator.validate(new RestablecerContrasenaRequest("tok", "a".repeat(72)))).isEmpty();
    }

    @Test
    void rechaza_contrasena_fuera_de_rango() {
        assertThat(validator.validate(new RestablecerContrasenaRequest("tok", "1234567"))).hasSize(1);
        assertThat(validator.validate(new RestablecerContrasenaRequest("tok", "a".repeat(73)))).hasSize(1);
    }

    @Test
    void rechaza_token_vacio() {
        assertThat(validator.validate(new RestablecerContrasenaRequest(" ", "12345678"))).isNotEmpty();
    }

    @Test
    void olvide_contrasena_exige_un_email_valido() {
        assertThat(validator.validate(new OlvideContrasenaRequest("no-es-email"))).isNotEmpty();
        assertThat(validator.validate(new OlvideContrasenaRequest("ana@test.com"))).isEmpty();
    }
}
