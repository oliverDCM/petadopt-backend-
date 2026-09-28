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

@Component
public class JwtTokenAdapter implements TokenServicePort {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenAdapter(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generar(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.email())
                .claim("rol", usuario.rol().name())
                .claim("id", usuario.id())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<DatosToken> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Rol rol = Rol.valueOf(claims.get("rol", String.class));
            return Optional.of(new DatosToken(claims.getSubject(), rol));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}