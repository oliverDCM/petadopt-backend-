package com.petadopt.infrastructure.adapter.out.storage;

import com.petadopt.application.port.out.ImagenStoragePort;
import com.petadopt.domain.exception.AlmacenamientoImagenException;
import com.petadopt.domain.model.ImagenArchivo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Adaptador de salida que guarda las fotos en Amazon S3 usando su API REST (PUT Object) firmada con
 * AWS Signature V4. Funciona igual con cualquier servicio compatible con S3 (Cloudflare R2, MinIO)
 * cambiando S3_ENDPOINT. Devuelve la URL pública con la que el frontend muestra la foto y borra las fotos
 * que dejan de usarse.
 */
@Component
public class S3StorageAdapter implements ImagenStoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3StorageAdapter.class);
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(20);

    private final HttpClient http;
    private final Clock clock;
    private final Supplier<String> generadorNombres;
    private final FirmaAwsV4 firma;
    private final String bucket;
    private final String carpeta;
    private final URI endpoint;
    private final String urlPublica;
    private final boolean configurado;

    @Autowired
    public S3StorageAdapter(@Value("${s3.region}") String region,
                            @Value("${s3.bucket}") String bucket,
                            @Value("${s3.access-key}") String accessKey,
                            @Value("${s3.secret-key}") String secretKey,
                            @Value("${s3.endpoint}") String endpoint,
                            @Value("${s3.public-url}") String urlPublica,
                            @Value("${s3.folder}") String carpeta,
                            Clock clock) {
        this(HttpClient.newBuilder().connectTimeout(TIEMPO_MAXIMO).build(), clock,
                () -> UUID.randomUUID().toString(),
                region, bucket, accessKey, secretKey, endpoint, urlPublica, carpeta);
    }

    S3StorageAdapter(HttpClient http, Clock clock, Supplier<String> generadorNombres,
                     String region, String bucket, String accessKey, String secretKey,
                     String endpoint, String urlPublica, String carpeta) {
        this.http = http;
        this.clock = clock;
        this.generadorNombres = generadorNombres;
        this.firma = new FirmaAwsV4(accessKey, secretKey, region);
        this.bucket = bucket;
        this.carpeta = quitarBarras(carpeta);
        this.endpoint = URI.create(endpoint.isBlank()
                ? "https://s3." + region + ".amazonaws.com"
                : quitarBarraFinal(endpoint));
        this.urlPublica = urlPublica.isBlank()
                ? (endpoint.isBlank()
                    ? "https://" + bucket + ".s3." + region + ".amazonaws.com"
                    : this.endpoint + "/" + bucket)
                : quitarBarraFinal(urlPublica);
        this.configurado = !(bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank() || region.isBlank());
        if (!configurado) {
            log.warn("S3 no está configurado (S3_REGION, S3_BUCKET, S3_ACCESS_KEY, S3_SECRET_KEY): "
                    + "la subida de fotos responderá 502");
        }
    }

    @Override
    public String subirImagen(ImagenArchivo imagen) {
        if (!configurado) {
            throw new AlmacenamientoImagenException(new IllegalStateException("S3 no está configurado"));
        }
        String llave = (carpeta.isEmpty() ? "" : carpeta + "/") + generadorNombres.get() + "." + extension(imagen.getContentType());
        String ruta = FirmaAwsV4.codificarRuta("/" + bucket + "/" + llave);
        URI destino = URI.create(endpoint + ruta);

        FirmaAwsV4.Firma f = firma.firmar("PUT", host(destino), ruta, imagen.getContenido(), clock.instant());
        HttpRequest peticion = HttpRequest.newBuilder(destino)
                .timeout(TIEMPO_MAXIMO)
                .header("Content-Type", imagen.getContentType())
                .header("x-amz-date", f.amzDate())
                .header("x-amz-content-sha256", f.hashContenido())
                .header("Authorization", f.authorization())
                .PUT(HttpRequest.BodyPublishers.ofByteArray(imagen.getContenido()))
                .build();

        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new AlmacenamientoImagenException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AlmacenamientoImagenException(e);
        }
        if (respuesta.statusCode() / 100 != 2) {
            throw new AlmacenamientoImagenException(new IllegalStateException(
                    "S3 respondió " + respuesta.statusCode() + ": " + resumir(respuesta.body())));
        }
        log.info("Foto subida a S3: {}", llave);
        return urlPublica + "/" + FirmaAwsV4.codificarRuta(llave);
    }

    @Override
    public boolean esUrlPropia(String url) {
        return url != null && url.startsWith(prefijoPropio());
    }

    @Override
    public void eliminarImagen(String url) {
        if (!configurado || !esUrlPropia(url)) {
            return;
        }
        String llave = url.substring(urlPublica.length() + 1);
        if (llave.contains("..") || llave.contains("?") || llave.contains("#")) {
            return; // nunca se firma un borrado para algo que no sea una llave simple dentro de la carpeta de fotos
        }
        String ruta = "/" + bucket + "/" + llave; // la llave viene de una URL: ya está codificada
        URI destino = URI.create(endpoint + ruta);
        byte[] sinCuerpo = new byte[0];
        FirmaAwsV4.Firma f = firma.firmar("DELETE", host(destino), ruta, sinCuerpo, clock.instant());
        HttpRequest peticion = HttpRequest.newBuilder(destino)
                .timeout(TIEMPO_MAXIMO)
                .header("x-amz-date", f.amzDate())
                .header("x-amz-content-sha256", f.hashContenido())
                .header("Authorization", f.authorization())
                .DELETE()
                .build();
        try {
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() / 100 == 2) {
                log.info("Foto eliminada de S3: {}", llave);
            } else {
                log.warn("S3 respondió {} al eliminar {}: {}", respuesta.statusCode(), llave, resumir(respuesta.body()));
            }
        } catch (IOException e) {
            log.warn("No se pudo eliminar {} de S3 ({})", llave, e.getClass().getSimpleName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Se interrumpió la eliminación de {} en S3", llave);
        }
    }

    /** Las fotos propias viven bajo <url pública>/<carpeta>/. */
    private String prefijoPropio() {
        return urlPublica + "/" + (carpeta.isEmpty() ? "" : FirmaAwsV4.codificarRuta(carpeta) + "/");
    }

    static String extension(String contentType) {
        if (contentType == null) {
            return "img";
        }
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "img";
        };
    }

    /** Valor de la cabecera Host tal como lo manda HttpClient: incluye el puerto solo si no es el de por defecto. */
    private static String host(URI uri) {
        return uri.getPort() == -1 ? uri.getHost() : uri.getHost() + ":" + uri.getPort();
    }

    private static String resumir(String cuerpo) {
        if (cuerpo == null) {
            return "";
        }
        return cuerpo.length() > 300 ? cuerpo.substring(0, 300) + "…" : cuerpo;
    }

    private static String quitarBarraFinal(String texto) {
        return texto.endsWith("/") ? texto.substring(0, texto.length() - 1) : texto;
    }

    private static String quitarBarras(String texto) {
        String t = texto;
        while (t.startsWith("/")) {
            t = t.substring(1);
        }
        while (t.endsWith("/")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }
}
