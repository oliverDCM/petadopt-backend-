package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.TipoPublicacion;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "animales")
public class AnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EspecieAnimal especie;

    @Column(length = 80)
    private String raza;

    @Column(name = "edad_meses")
    private Integer edadMeses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SexoAnimal sexo;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAnimal estado;

    @Column(length = 80)
    private String ciudad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private UsuarioEntity vendedor;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean vacunado;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean esterilizado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "varchar(10) default 'ADOPCION'")
    private TipoPublicacion tipo;

    protected AnimalEntity() {
    }

    public AnimalEntity(Long id, String nombre, EspecieAnimal especie, String raza,
                        Integer edadMeses, SexoAnimal sexo, String descripcion,
                        String fotoUrl, EstadoAnimal estado, String ciudad,
                        UsuarioEntity vendedor, LocalDateTime fechaPublicacion,
                        boolean vacunado, boolean esterilizado, TipoPublicacion tipo) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.edadMeses = edadMeses;
        this.sexo = sexo;
        this.descripcion = descripcion;
        this.fotoUrl = fotoUrl;
        this.estado = estado;
        this.ciudad = ciudad;
        this.vendedor = vendedor;
        this.fechaPublicacion = fechaPublicacion;
        this.vacunado = vacunado;
        this.esterilizado = esterilizado;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public EspecieAnimal getEspecie() { return especie; }
    public String getRaza() { return raza; }
    public Integer getEdadMeses() { return edadMeses; }
    public SexoAnimal getSexo() { return sexo; }
    public String getDescripcion() { return descripcion; }
    public String getFotoUrl() { return fotoUrl; }
    public EstadoAnimal getEstado() { return estado; }
    public String getCiudad() { return ciudad; }
    public UsuarioEntity getVendedor() { return vendedor; }
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public boolean isVacunado() { return vacunado; }
    public boolean isEsterilizado() { return esterilizado; }
    public TipoPublicacion getTipo() { return tipo; }
}