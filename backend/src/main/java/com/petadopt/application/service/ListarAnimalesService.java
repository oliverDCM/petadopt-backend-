package com.petadopt.application.service;

import com.petadopt.application.port.in.ListarAnimalesUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarAnimalesService implements ListarAnimalesUseCase {

    private final AnimalRepositoryPort animalRepository;

    public ListarAnimalesService(AnimalRepositoryPort animalRepository) {
        this.animalRepository = animalRepository;
    }

    @Override
    public List<Animal> listarDisponibles() {
        return animalRepository.listarPorEstado(EstadoAnimal.DISPONIBLE);
    }
}