package com.petadopt.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_recuperacion",
        indexes = @Index(name = "idx_tokens_recuperacion_usuario", columnList = "usuario_id"))
public class TokenRecuperacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    /** Hash SHA-256 en hexadecimal: siempre 64 caracteres. */
    @Column(name = "token_hash", nullable = false, unique = true, columnDefinition = "CHAR(64)")
    private String tokenHash;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean usado;

    protected TokenRecuperacionEntity() {
    }

    public TokenRecuperacionEntity(Long id, UsuarioEntity usuario, String tokenHash,
                                   LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion, boolean usado) {
        this.id = id;
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.fechaCreacion = fechaCreacion;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    public Long getId() { return id; }
    public UsuarioEntity getUsuario() { return usuario; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public boolean isUsado() { return usado; }
}
