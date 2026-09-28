package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.domain.model.Rol;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20)
    private String telefono;

    @Column(length = 80)
    private String ciudad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    protected UsuarioEntity() {
        // requerido por JPA
    }

    public UsuarioEntity(Long id, String nombre, String email, String password,
                         String telefono, String ciudad, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.ciudad = ciudad;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getTelefono() { return telefono; }
    public String getCiudad() { return ciudad; }
    public Rol getRol() { return rol; }
}