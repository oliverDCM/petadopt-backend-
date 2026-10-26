package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.domain.model.EstadoSolicitud;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
public class SolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private AnimalEntity animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private UsuarioEntity solicitante;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_decision")
    private LocalDateTime fechaDecision;

    protected SolicitudEntity() {
    }

    public SolicitudEntity(Long id, AnimalEntity animal, UsuarioEntity solicitante,
                           String mensaje, EstadoSolicitud estado,
                           LocalDateTime fechaSolicitud, LocalDateTime fechaDecision) {
        this.id = id;
        this.animal = animal;
        this.solicitante = solicitante;
        this.mensaje = mensaje;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaDecision = fechaDecision;
    }

    public Long getId() { return id; }
    public AnimalEntity getAnimal() { return animal; }
    public UsuarioEntity getSolicitante() { return solicitante; }
    public String getMensaje() { return mensaje; }
    public EstadoSolicitud getEstado() { return estado; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public LocalDateTime getFechaDecision() { return fechaDecision; }
}