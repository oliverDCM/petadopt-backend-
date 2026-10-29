package com.petadopt.application.service;

import com.petadopt.application.port.in.EditarAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.ImagenStoragePort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoDisponibleException;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.ImagenArchivo;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarAnimalService implements EditarAnimalUseCase {

    private static final String SOLO_DUENO = "Solo el vendedor dueño puede editar el animal";

    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ImagenStoragePort imagenStorage;

    public EditarAnimalService(AnimalRepositoryPort animalRepository,
                               UsuarioRepositoryPort usuarioRepository,
                               ImagenStoragePort imagenStorage) {
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
        this.imagenStorage = imagenStorage;
    }

    @Override
    @Transactional
    public Animal editar(Comando c) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(c.vendedorEmail())
                .orElseThrow(() -> new NoEsDuenoDelAnimalException(SOLO_DUENO));

        Animal animal = animalRepository.buscarPorIdParaActualizar(c.animalId())
                .orElseThrow(() -> new AnimalNoEncontradoException(c.animalId()));

        if (!animal.vendedorId().equals(vendedor.id())) {
            throw new NoEsDuenoDelAnimalException(SOLO_DUENO);
        }
        if (animal.estado() != EstadoAnimal.DISPONIBLE) {
            throw new AnimalNoDisponibleException();
        }

        String fotoUrl = resolverFotoUrl(c, animal);
        return animalRepository.guardar(animal.conDatos(
                c.nombre().trim(), c.especie(), c.raza(), c.edadMeses(), c.sexo(),
                c.descripcion(), fotoUrl, c.ciudad(), c.vacunado(), c.esterilizado(), c.tipo()));
    }

    /** Foto nueva: se valida y se sube. URL nueva: se usa. Nada: se conserva la actual. */
    private String resolverFotoUrl(Comando c, Animal actual) {
        ImagenArchivo foto = c.foto();
        if (foto != null && !foto.estaVacia()) {
            ValidacionImagen.validar(foto);
            return imagenStorage.subirImagen(foto);
        }
        if (c.fotoUrl() != null && !c.fotoUrl().isBlank()) {
            return c.fotoUrl();
        }
        return actual.fotoUrl();
    }
}
