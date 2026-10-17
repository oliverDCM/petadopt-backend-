package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class AnimalPersistenceAdapter implements AnimalRepositoryPort {

    private final AnimalJpaRepository jpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;

    public AnimalPersistenceAdapter(AnimalJpaRepository jpaRepository,
                                    UsuarioJpaRepository usuarioJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Animal guardar(Animal animal) {
        AnimalEntity guardado = jpaRepository.save(toEntity(animal));
        return toDomain(guardado);
    }

    @Override
    public Optional<Animal> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Animal> listarPorEstado(EstadoAnimal estado) {
        return jpaRepository.findByEstado(estado).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Animal> listarPorVendedor(Long vendedorId) {
        return jpaRepository.findByVendedorId(vendedorId).stream().map(this::toDomain).toList();
    }

    private AnimalEntity toEntity(Animal a) {
        UsuarioEntity vendedor = usuarioJpaRepository.getReferenceById(a.vendedorId());
        LocalDateTime fecha = a.fechaPublicacion() != null ? a.fechaPublicacion() : LocalDateTime.now();
        EstadoAnimal estado = a.estado() != null ? a.estado() : EstadoAnimal.DISPONIBLE;
        return new AnimalEntity(a.id(), a.nombre(), a.especie(), a.raza(), a.edadMeses(),
                a.sexo(), a.descripcion(), a.fotoUrl(), estado, a.ciudad(), vendedor, fecha);
    }

    private Animal toDomain(AnimalEntity e) {
        return new Animal(e.getId(), e.getNombre(), e.getEspecie(), e.getRaza(), e.getEdadMeses(),
                e.getSexo(), e.getDescripcion(), e.getFotoUrl(), e.getEstado(), e.getCiudad(),
                e.getVendedor().getId(), e.getFechaPublicacion());
    }
}