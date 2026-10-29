package com.petadopt.application.service;

import com.petadopt.application.port.in.ConsultarAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarAnimalService implements ConsultarAnimalUseCase {

    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public ConsultarAnimalService(AnimalRepositoryPort animalRepository, UsuarioRepositoryPort usuarioRepository) {
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Detalle consultar(Long animalId) {
        Animal animal = animalRepository.buscarPorId(animalId)
                .orElseThrow(() -> new AnimalNoEncontradoException(animalId));
        Usuario vendedor = usuarioRepository.buscarPorId(animal.vendedorId()).orElse(null);
        return new Detalle(animal,
                vendedor != null ? vendedor.nombre() : null,
                vendedor != null ? vendedor.ciudad() : null);
    }
}
