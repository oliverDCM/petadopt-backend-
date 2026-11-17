package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.Pagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import com.petadopt.domain.model.TipoPublicacion;

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
    public Optional<Animal> buscarPorIdParaActualizar(Long id) {
        return jpaRepository.buscarBloqueadoPorId(id).map(this::toDomain);
    }

    @Override
    public Pagina<Animal> buscar(EstadoAnimal estado, EspecieAnimal especie, TipoPublicacion tipo,
                                 String ciudadNormalizada, int pagina, int tamano) {
        // El id desempata animales publicados en el mismo instante: el orden entre páginas es estable
        Sort orden = Sort.by(Sort.Order.desc("fechaPublicacion"), Sort.Order.desc("id"));
        Page<AnimalEntity> resultado = jpaRepository.buscarCatalogo(estado, especie, tipo,
                patronLike(ciudadNormalizada), PageRequest.of(pagina, tamano, orden));
        return new Pagina<>(resultado.getContent().stream().map(this::toDomain).toList(),
                resultado.getTotalElements(), pagina, tamano);
    }

    /** "%texto%" escapando los comodines que escriba el usuario (el carácter de escape es '!'). */
    static String patronLike(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return "%" + texto.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    @Override
    public List<Animal> listarPorVendedor(Long vendedorId) {
        return jpaRepository.findByVendedorIdOrderByFechaPublicacionDescIdDesc(vendedorId)
                .stream().map(this::toDomain).toList();
    }

    private AnimalEntity toEntity(Animal a) {
        UsuarioEntity vendedor = usuarioJpaRepository.getReferenceById(a.vendedorId());
        LocalDateTime fecha = a.fechaPublicacion() != null ? a.fechaPublicacion() : LocalDateTime.now();
        EstadoAnimal estado = a.estado() != null ? a.estado() : EstadoAnimal.DISPONIBLE;
        TipoPublicacion tipo = a.tipo() != null ? a.tipo() : TipoPublicacion.ADOPCION;
        return new AnimalEntity(a.id(), a.nombre(), a.especie(), a.raza(), a.edadMeses(),
                a.sexo(), a.descripcion(), a.fotoUrl(), estado, a.ciudad(), vendedor, fecha,
                a.vacunado(), a.esterilizado(), tipo);
    }

    private Animal toDomain(AnimalEntity e) {
        return new Animal(e.getId(), e.getNombre(), e.getEspecie(), e.getRaza(), e.getEdadMeses(),
                e.getSexo(), e.getDescripcion(), e.getFotoUrl(), e.getEstado(), e.getCiudad(),
                e.getVendedor().getId(), e.getFechaPublicacion(),
                e.isVacunado(), e.isEsterilizado(), e.getTipo());
    }
}