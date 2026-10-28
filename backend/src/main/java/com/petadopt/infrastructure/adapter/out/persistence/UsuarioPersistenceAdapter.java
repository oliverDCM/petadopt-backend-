package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioPersistenceAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity guardada = jpaRepository.save(toEntity(usuario));
        return toDomain(guardada);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    private UsuarioEntity toEntity(Usuario u) {
        return new UsuarioEntity(u.id(), u.nombre(), u.email(), u.password(),
                u.telefono(), u.ciudad(), u.rol());
    }

    private Usuario toDomain(UsuarioEntity e) {
        return new Usuario(e.getId(), e.getNombre(), e.getEmail(), e.getPassword(),
                e.getTelefono(), e.getCiudad(), e.getRol());
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }
}