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
import java.util.Date;
import java.util.Optional;

/** Adaptador de salida que implementa TokenServicePort con JWT (jjwt 0.12.x, firma HS256). */
@Component
public class JwtTokenAdapter implements TokenServicePort {

    private static final int LONGITUD_MINIMA_SECRETO = 32;

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
                    Rol.valueOf(claims.get("rol", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            // firma inválida, token vencido, mal formado o con datos inesperados
            return Optional.empty();
        }
    }
}
