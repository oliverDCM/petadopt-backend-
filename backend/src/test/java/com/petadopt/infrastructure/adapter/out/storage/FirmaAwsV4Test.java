package com.petadopt.infrastructure.adapter.out.storage;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FirmaAwsV4Test {

    private static final byte[] CUERPO = "foto-de-firulais".getBytes(StandardCharsets.UTF_8);

    @Test
    void generaLaMismaFirmaQueElSdkOficialDeAws() {
        // Valor esperado calculado con botocore (SDK oficial de AWS para Python) con los mismos datos
        FirmaAwsV4 firma = new FirmaAwsV4("AKIAEJEMPLO", "secretoDeEjemplo123", "us-east-1");

        FirmaAwsV4.Firma f = firma.firmar("PUT", "s3.us-east-1.amazonaws.com",
                "/petadopt-fotos/petadopt/animales/abc-123.jpg", CUERPO, Instant.parse("2026-11-03T21:53:42Z"));

        assertThat(f.amzDate()).isEqualTo("20261103T215342Z");
        assertThat(f.hashContenido()).isEqualTo(FirmaAwsV4.sha256Hex(CUERPO));
        assertThat(f.authorization()).isEqualTo("AWS4-HMAC-SHA256 "
                + "Credential=AKIAEJEMPLO/20261103/us-east-1/s3/aws4_request, "
                + "SignedHeaders=host;x-amz-content-sha256;x-amz-date, "
                + "Signature=5182fb8615ef8040f32bb6210627eaae078fdbf9dd0aed217b6a2db5cbf54290");
    }

    @Test
    void laFirmaCambiaSiCambiaElContenido() {
        FirmaAwsV4 firma = new FirmaAwsV4("AKIAEJEMPLO", "secretoDeEjemplo123", "us-east-1");
        Instant momento = Instant.parse("2026-11-03T21:53:42Z");

        String original = firma.firmar("PUT", "h", "/b/k", CUERPO, momento).authorization();
        String alterada = firma.firmar("PUT", "h", "/b/k", "otra".getBytes(StandardCharsets.UTF_8), momento)
                .authorization();

        assertThat(alterada).isNotEqualTo(original);
    }

    @Test
    void codificaLaRutaConservandoLasBarras() {
        assertThat(FirmaAwsV4.codificarRuta("/b/fotos/mi foto ñ.jpg")).isEqualTo("/b/fotos/mi%20foto%20%C3%B1.jpg");
        assertThat(FirmaAwsV4.codificarRuta("/b/a-b_c.d~e/1.png")).isEqualTo("/b/a-b_c.d~e/1.png");
    }
}
