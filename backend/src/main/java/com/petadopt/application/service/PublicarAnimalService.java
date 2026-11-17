package com.petadopt.application.service;

import com.petadopt.application.port.in.PublicarAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.ImagenStoragePort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.VendedorNoAutorizadoException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.ImagenArchivo;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PublicarAnimalService implements PublicarAnimalUseCase {

    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final GestorFotos fotos;

    public PublicarAnimalService(AnimalRepositoryPort animalRepository,
                                 UsuarioRepositoryPort usuarioRepository,
                                 ImagenStoragePort imagenStorage) {
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
        this.fotos = new GestorFotos(imagenStorage);
    }

    @Override
    public Animal publicar(Comando comando) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(comando.vendedorEmail())
                .filter(u -> u.rol() == Rol.VENDEDOR)
                .orElseThrow(VendedorNoAutorizadoException::new);

        String enlace = fotos.urlPermitida(comando.fotoUrl(), null);
        String subida = fotos.subirSiHay(comando.foto());

        Animal nuevo = new Animal(
                null,
                comando.nombre().trim(),
                comando.especie(),
                comando.raza(),
                comando.edadMeses(),
                comando.sexo(),
                comando.descripcion(),
                subida != null ? subida : enlace,
                EstadoAnimal.DISPONIBLE,
                comando.ciudad(),
                vendedor.id(),
                LocalDateTime.now(),
                comando.vacunado(),
                comando.esterilizado(),
                comando.tipo()
        );
        try {
            return animalRepository.guardar(nuevo);
        } catch (RuntimeException e) {
            fotos.descartar(subida); // la publicación no se guardó: la foto recién subida quedaría huérfana
            throw e;
        }
    }
}
