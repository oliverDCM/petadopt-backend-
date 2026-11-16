package com.petadopt.infrastructure.adapter.out.storage;

import com.petadopt.domain.exception.AlmacenamientoImagenException;
import com.petadopt.domain.model.ImagenArchivo;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Prueba el adaptador contra un servidor HTTP local que hace de S3 (no sale a internet). */
class S3StorageAdapterTest {

    private static final byte[] FOTO = "foto-de-firulais".getBytes(StandardCharsets.UTF_8);
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-03T21:53:42Z"), ZoneOffset.UTC);

    private HttpServer servidor;
    private final Map<String, String> recibido = new ConcurrentHashMap<>();
    private final AtomicInteger codigoRespuesta = new AtomicInteger(200);
    private String endpoint;

    @BeforeEach
    void levantarS3Falso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            recibido.put("metodo", intercambio.getRequestMethod());
            recibido.put("ruta", intercambio.getRequestURI().getRawPath());
            recibido.put("host", intercambio.getRequestHeaders().getFirst("Host"));
            recibido.put("contentType", intercambio.getRequestHeaders().getFirst("Content-Type"));
            recibido.put("authorization", intercambio.getRequestHeaders().getFirst("Authorization"));
            recibido.put("amzDate", intercambio.getRequestHeaders().getFirst("x-amz-date"));
            recibido.put("cuerpo", new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] respuesta = codigoRespuesta.get() == 200
                    ? new byte[0]
                    : "<Error><Code>AccessDenied</Code></Error>".getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(codigoRespuesta.get(), respuesta.length == 0 ? -1 : respuesta.length);
            if (respuesta.length > 0) {
                intercambio.getResponseBody().write(respuesta);
            }
            intercambio.close();
        });
        servidor.start();
        endpoint = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void apagar() {
        servidor.stop(0);
    }

    private S3StorageAdapter adaptador(String endpoint, String urlPublica) {
        return new S3StorageAdapter(HttpClient.newHttpClient(), RELOJ, () -> "abc-123",
                "us-east-1", "petadopt-fotos", "AKIAEJEMPLO", "secretoDeEjemplo123",
                endpoint, urlPublica, "/petadopt/animales/");
    }

    @Test
    void subeLaFotoConPutFirmadoYDevuelveLaUrlPublica() {
        String url = adaptador(endpoint + "/", "").subirImagen(new ImagenArchivo("f.jpg", "image/jpeg", FOTO));

        assertThat(url).isEqualTo(endpoint + "/petadopt-fotos/petadopt/animales/abc-123.jpg");
        assertThat(recibido)
                .containsEntry("metodo", "PUT")
                .containsEntry("ruta", "/petadopt-fotos/petadopt/animales/abc-123.jpg")
                .containsEntry("host", "127.0.0.1:" + servidor.getAddress().getPort())
                .containsEntry("contentType", "image/jpeg")
                .containsEntry("amzDate", "20261103T215342Z")
                .containsEntry("cuerpo", "foto-de-firulais");
        assertThat(recibido.get("authorization"))
                .startsWith("AWS4-HMAC-SHA256 Credential=AKIAEJEMPLO/20261103/us-east-1/s3/aws4_request");
    }

    @Test
    void usaLaUrlPublicaConfiguradaSiExiste() {
        String url = adaptador(endpoint, "https://fotos.petadopt.dev/")
                .subirImagen(new ImagenArchivo("f.png", "image/png", FOTO));

        assertThat(url).isEqualTo("https://fotos.petadopt.dev/petadopt/animales/abc-123.png");
    }

    @Test
    void siS3RespondeErrorLanzaAlmacenamientoImagenException() {
        codigoRespuesta.set(403);
        S3StorageAdapter s3 = adaptador(endpoint, "");
        ImagenArchivo foto = new ImagenArchivo("f.webp", "image/webp", FOTO);

        assertThatThrownBy(() -> s3.subirImagen(foto))
                .isInstanceOf(AlmacenamientoImagenException.class)
                .cause().hasMessageContaining("403").hasMessageContaining("AccessDenied");
    }

    @Test
    void siS3NoRespondeLanzaAlmacenamientoImagenException() {
        S3StorageAdapter s3 = adaptador(endpoint, "");
        servidor.stop(0);
        ImagenArchivo foto = new ImagenArchivo("f.jpg", "image/jpeg", FOTO);

        assertThatThrownBy(() -> s3.subirImagen(foto)).isInstanceOf(AlmacenamientoImagenException.class);
    }

    @Test
    void sinCredencialesNoIntentaSubir() {
        S3StorageAdapter s3 = new S3StorageAdapter(HttpClient.newHttpClient(), RELOJ, () -> "x",
                "us-east-1", "", "", "", endpoint, "", "petadopt/animales");
        ImagenArchivo foto = new ImagenArchivo("f.jpg", "image/jpeg", FOTO);

        assertThatThrownBy(() -> s3.subirImagen(foto))
                .isInstanceOf(AlmacenamientoImagenException.class)
                .cause().hasMessageContaining("no está configurado");
        assertThat(recibido).isEmpty();
    }

    @Test
    void elConstructorDeSpringSinCredencialesRespondeError() {
        S3StorageAdapter s3 = new S3StorageAdapter("us-east-1", "", "", "", "", "", "petadopt/animales", RELOJ);
        ImagenArchivo foto = new ImagenArchivo("f.jpg", "image/jpeg", FOTO);

        assertThatThrownBy(() -> s3.subirImagen(foto)).isInstanceOf(AlmacenamientoImagenException.class);
    }

    @Test
    void eligeLaExtensionSegunElTipoDeImagen() {
        assertThat(S3StorageAdapter.extension("image/jpeg")).isEqualTo("jpg");
        assertThat(S3StorageAdapter.extension("IMAGE/PNG")).isEqualTo("png");
        assertThat(S3StorageAdapter.extension("image/webp")).isEqualTo("webp");
        assertThat(S3StorageAdapter.extension("image/gif")).isEqualTo("img");
        assertThat(S3StorageAdapter.extension(null)).isEqualTo("img");
    }
}
