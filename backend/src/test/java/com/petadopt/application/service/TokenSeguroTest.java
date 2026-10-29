package com.petadopt.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenSeguroTest {

    @Test
    void genera_tokens_distintos_aptos_para_url() {
        String a = TokenSeguro.generar();
        String b = TokenSeguro.generar();
        assertThat(a).isNotEqualTo(b).hasSize(43).matches("[A-Za-z0-9_-]+");
    }

    @Test
    void el_hash_es_sha256_en_hexadecimal() {
        // SHA-256 de "abc" (vector de prueba oficial)
        assertThat(TokenSeguro.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
