package com.petadopt.application.port.out;

import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.Pagina;
import com.petadopt.domain.model.TipoPublicacion;

import java.util.List;
import java.util.Optional;

public interface AnimalRepositoryPort {

    Animal guardar(Animal animal);

    Optional<Animal> buscarPorId(Long id);

    /** Igual que buscarPorId, pero bloquea la fila hasta el fin de la transacción (evita carreras). */
    Optional<Animal> buscarPorIdParaActualizar(Long id);

    /**
     * Página del catálogo: animales en el estado dado, del más reciente al más antiguo.
     * Los filtros nulos no se aplican; {@code ciudadNormalizada} va en minúsculas y sin tildes.
     */
    Pagina<Animal> buscar(EstadoAnimal estado, EspecieAnimal especie, TipoPublicacion tipo,
                          String ciudadNormalizada, int pagina, int tamano);

    List<Animal> listarPorVendedor(Long vendedorId);
}