package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.application.port.out.TokenRecuperacionRepositoryPort;
import com.petadopt.domain.model.TokenRecuperacion;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class TokenRecuperacionPersistenceAdapter implements TokenRecuperacionRepositoryPort {

    private final TokenRecuperacionJpaRepository jpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;

    public TokenRecuperacionPersistenceAdapter(TokenRecuperacionJpaRepository jpaRepository,
                                               UsuarioJpaRepository usuarioJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public TokenRecuperacion guardar(TokenRecuperacion t) {
        TokenRecuperacionEntity entidad = new TokenRecuperacionEntity(t.id(),
                usuarioJpaRepository.getReferenceById(t.usuarioId()), t.tokenHash(),
                t.fechaCreacion(), t.fechaExpiracion(), t.usado());
        return toDomain(jpaRepository.save(entidad));
    }

    @Override
    public Optional<TokenRecuperacion> buscarPorHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public int anularVigentesDeUsuario(Long usuarioId) {
        return jpaRepository.anularVigentesDeUsuario(usuarioId);
    }

    @Override
    @Transactional
    public int eliminarVencidos(LocalDateTime ahora) {
        return jpaRepository.eliminarVencidos(ahora);
    }

    private TokenRecuperacion toDomain(TokenRecuperacionEntity e) {
        return new TokenRecuperacion(e.getId(), e.getUsuario().getId(), e.getTokenHash(),
                e.getFechaCreacion(), e.getFechaExpiracion(), e.isUsado());
    }
}
