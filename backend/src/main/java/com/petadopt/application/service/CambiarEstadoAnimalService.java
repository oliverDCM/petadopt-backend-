package com.petadopt.application.service;

import com.petadopt.application.port.in.CambiarEstadoAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.exception.TransicionEstadoInvalidaException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CambiarEstadoAnimalService implements CambiarEstadoAnimalUseCase {

    private static final String SOLO_DUENO = "Solo el vendedor dueño puede cambiar el estado del animal";

    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public CambiarEstadoAnimalService(AnimalRepositoryPort animalRepository,
                                      UsuarioRepositoryPort usuarioRepository) {
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public Animal cambiar(Comando comando) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(comando.vendedorEmail())
                .orElseThrow(() -> new NoEsDuenoDelAnimalException(SOLO_DUENO));

        Animal animal = animalRepository.buscarPorIdParaActualizar(comando.animalId())
                .orElseThrow(() -> new AnimalNoEncontradoException(comando.animalId()));

        if (!animal.vendedorId().equals(vendedor.id())) {
            throw new NoEsDuenoDelAnimalException(SOLO_DUENO);
        }
        if (!animal.estado().puedePasarA(comando.nuevoEstado())) {
            throw new TransicionEstadoInvalidaException(animal.estado(), comando.nuevoEstado());
        }
        return animalRepository.guardar(animal.conEstado(comando.nuevoEstado()));
    }
}
