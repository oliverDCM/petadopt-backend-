package com.petadopt.infrastructure.adapter.out.storage;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

/**
 * Firma AWS Signature Version 4 para peticiones a S3 (o a cualquier servicio compatible con S3).
 * Firma solo las cabeceras host, x-amz-content-sha256 y x-amz-date, que es lo mínimo que exige S3.
 * Referencia: https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_sigv-create-signed-request.html
 */
final class FirmaAwsV4 {

    static final String ALGORITMO = "AWS4-HMAC-SHA256";
    static final String CABECERAS_FIRMADAS = "host;x-amz-content-sha256;x-amz-date";
    private static final String SERVICIO = "s3";
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final String accessKey;
    private final String secretKey;
    private final String region;

    FirmaAwsV4(String accessKey, String secretKey, String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    /** Resultado de firmar: las cabeceras que hay que mandar junto con la petición. */
    record Firma(String amzDate, String hashContenido, String authorization) {
    }

    Firma firmar(String metodo, String host, String rutaCodificada, byte[] contenido, Instant momento) {
        String amzDate = FORMATO_FECHA_HORA.format(momento);
        String fecha = FORMATO_FECHA.format(momento);
        String hashContenido = sha256Hex(contenido);

        String peticionCanonica = metodo + "\n"
                + rutaCodificada + "\n"
                + "\n" // sin query string
                + "host:" + host + "\n"
                + "x-amz-content-sha256:" + hashContenido + "\n"
                + "x-amz-date:" + amzDate + "\n"
                + "\n"
                + CABECERAS_FIRMADAS + "\n"
                + hashContenido;

        String alcance = fecha + "/" + region + "/" + SERVICIO + "/aws4_request";
        String textoAFirmar = ALGORITMO + "\n" + amzDate + "\n" + alcance + "\n"
                + sha256Hex(peticionCanonica.getBytes(StandardCharsets.UTF_8));

        byte[] llave = hmac(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), fecha);
        llave = hmac(llave, region);
        llave = hmac(llave, SERVICIO);
        llave = hmac(llave, "aws4_request");
        String firma = HexFormat.of().formatHex(hmac(llave, textoAFirmar));

        String authorization = ALGORITMO + " Credential=" + accessKey + "/" + alcance
                + ", SignedHeaders=" + CABECERAS_FIRMADAS + ", Signature=" + firma;
        return new Firma(amzDate, hashContenido, authorization);
    }

    /** Codifica cada segmento de la ruta según RFC 3986, conservando las barras. */
    static String codificarRuta(String ruta) {
        StringBuilder sb = new StringBuilder();
        for (byte b : ruta.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xFF);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.' || c == '~' || c == '/') {
                sb.append(c);
            } else {
                sb.append('%').append(String.format("%02X", b & 0xFF));
            }
        }
        return sb.toString();
    }

    static String sha256Hex(byte[] datos) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(datos));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private static byte[] hmac(byte[] llave, String texto) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(llave, "HmacSHA256"));
            return mac.doFinal(texto.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 no disponible", e);
        }
    }
}
