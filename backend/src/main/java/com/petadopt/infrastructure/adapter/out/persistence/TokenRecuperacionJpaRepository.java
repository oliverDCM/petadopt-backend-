package com.petadopt.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacionJpaRepository extends JpaRepository<TokenRecuperacionEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TokenRecuperacionEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update TokenRecuperacionEntity t set t.usado = true where t.usuario.id = :usuarioId and t.usado = false")
    int anularVigentesDeUsuario(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("delete from TokenRecuperacionEntity t where t.fechaExpiracion < :ahora")
    int eliminarVencidos(@Param("ahora") LocalDateTime ahora);
}
