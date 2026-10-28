package com.petadopt.application.service;

import com.petadopt.application.port.in.PublicarAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.ImagenStoragePort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.exception.VendedorNoAutorizadoException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.ImagenArchivo;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
public class PublicarAnimalService implements PublicarAnimalUseCase {

    private static final long TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ImagenStoragePort imagenStorage;

    public PublicarAnimalService(AnimalRepositoryPort animalRepository,
                                 UsuarioRepositoryPort usuarioRepository,
                                 ImagenStoragePort imagenStorage) {
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
        this.imagenStorage = imagenStorage;
    }

    @Override
    public Animal publicar(Comando comando) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(comando.vendedorEmail())
                .filter(u -> u.rol() == Rol.VENDEDOR)
                .orElseThrow(VendedorNoAutorizadoException::new);

        String fotoUrl = resolverFotoUrl(comando);

        Animal nuevo = new Animal(
                null,
                comando.nombre().trim(),
                comando.especie(),
                comando.raza(),
                comando.edadMeses(),
                comando.sexo(),
                comando.descripcion(),
                fotoUrl,
                EstadoAnimal.DISPONIBLE,
                comando.ciudad(),
                vendedor.id(),
                LocalDateTime.now(),
                comando.vacunado(),
                comando.esterilizado(),
                comando.tipo()
        );
        return animalRepository.guardar(nuevo);
    }

    private String resolverFotoUrl(Comando comando) {
        ImagenArchivo foto = comando.foto();
        if (foto == null || foto.estaVacia()) {
            return comando.fotoUrl();
        }
        validar(foto);
        return imagenStorage.subirImagen(foto);
    }

    private void validar(ImagenArchivo foto) {
        String tipo = foto.getContentType() == null ? "" : foto.getContentType().toLowerCase(Locale.ROOT);
        if (!TIPOS_PERMITIDOS.contains(tipo)) {
            throw new ImagenInvalidaException("Formato no permitido: use JPG, PNG o WEBP");
        }
        if (foto.tamanoBytes() > TAMANO_MAXIMO_BYTES) {
            throw new ImagenInvalidaException("La imagen supera el tamaño máximo de 5 MB");
        }
    }
}