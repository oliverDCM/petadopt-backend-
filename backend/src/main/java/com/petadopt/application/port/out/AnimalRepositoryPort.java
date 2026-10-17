package com.petadopt.application.port.out;

import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;

import java.util.List;
import java.util.Optional;

public interface AnimalRepositoryPort {

    Animal guardar(Animal animal);

    Optional<Animal> buscarPorId(Long id);

    List<Animal> listarPorEstado(EstadoAnimal estado);

    List<Animal> listarPorVendedor(Long vendedorId);
}