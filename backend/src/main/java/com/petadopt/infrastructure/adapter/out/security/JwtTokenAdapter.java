package com.petadopt.infrastructure.adapter.out.security;

import com.petadopt.application.port.out.TokenServicePort;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

/** Adaptador de salida que implementa TokenServicePort con JWT (jjwt 0.12.x, firma HS256). */
@Component
public class JwtTokenAdapter implements TokenServicePort {

    private static final int LONGITUD_MINIMA_SECRETO = 32;
    private static final String CLAIM_HUELLA = "cred";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtTokenAdapter(@Value("${jwt.secret}") String secreto,
                           @Value("${jwt.expiration-ms}") long expiracionMs) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < LONGITUD_MINIMA_SECRETO) {
            throw new IllegalStateException(
                    "jwt.secret debe tener al menos " + LONGITUD_MINIMA_SECRETO + " caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMs = expiracionMs;
    }

    @Override
    public String generar(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuario.id()))
                .claim("email", usuario.email())
                .claim("rol", usuario.rol().name())
                .claim(CLAIM_HUELLA, huellaCredencial(usuario))
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    @Override
    public Optional<DatosToken> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new DatosToken(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class),
                    Rol.valueOf(claims.get("rol", String.class)),
                    claims.get(CLAIM_HUELLA, String.class)));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            // firma inválida, token vencido, mal formado o con datos inesperados
            return Optional.empty();
        }
    }

    /** SHA-256 del hash BCrypt guardado, recortado: no permite recuperar ni la contraseña ni su hash. */
    @Override
    public String huellaCredencial(Usuario usuario) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(usuario.password().getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(digest, 12));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en la JVM", e);
        }
    }
}
