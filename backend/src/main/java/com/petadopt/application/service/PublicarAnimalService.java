package com.petadopt.application.service;

import com.petadopt.application.port.in.PublicarAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.VendedorNoAutorizadoException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PublicarAnimalService implements PublicarAnimalUseCase {

    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public PublicarAnimalService(AnimalRepositoryPort animalRepository,
                                 UsuarioRepositoryPort usuarioRepository) {
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Animal publicar(Comando comando) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(comando.vendedorEmail())
                .filter(u -> u.rol() == Rol.VENDEDOR)
                .orElseThrow(VendedorNoAutorizadoException::new);

        Animal nuevo = new Animal(
                null,
                comando.nombre().trim(),
                comando.especie(),
                comando.raza(),
                comando.edadMeses(),
                comando.sexo(),
                comando.descripcion(),
                comando.fotoUrl(),
                EstadoAnimal.DISPONIBLE,
                comando.ciudad(),
                vendedor.id(),
                LocalDateTime.now()
        );
        return animalRepository.guardar(nuevo);
    }
}