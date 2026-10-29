package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.domain.model.EstadoSolicitud;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "historial_solicitud",
        indexes = @Index(name = "idx_historial_solicitud_solicitud", columnList = "solicitud_id"))
public class HistorialSolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudEntity solicitud;

    /** Nulo cuando el registro corresponde a la creación de la solicitud. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoSolicitud estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoSolicitud estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected HistorialSolicitudEntity() {
    }

    public HistorialSolicitudEntity(Long id, SolicitudEntity solicitud, EstadoSolicitud estadoAnterior,
                                    EstadoSolicitud estadoNuevo, UsuarioEntity usuario, LocalDateTime fecha) {
        this.id = id;
        this.solicitud = solicitud;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuario = usuario;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public SolicitudEntity getSolicitud() { return solicitud; }
    public EstadoSolicitud getEstadoAnterior() { return estadoAnterior; }
    public EstadoSolicitud getEstadoNuevo() { return estadoNuevo; }
    public UsuarioEntity getUsuario() { return usuario; }
    public LocalDateTime getFecha() { return fecha; }
}
