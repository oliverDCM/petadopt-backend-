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
        if (!firmaCoincide(tipo, foto.getContenido())) {
            throw new ImagenInvalidaException("El archivo no es una imagen válida del formato indicado");
        }
    }

    /** Comprueba los primeros bytes del archivo: el Content-Type lo envía el cliente y se puede falsificar. */
    private static boolean firmaCoincide(String tipo, byte[] b) {
        return switch (tipo) {
            case "image/jpeg" -> b.length >= 3
                    && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            case "image/png" -> b.length >= 8
                    && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                    && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A;
            case "image/webp" -> b.length >= 12
                    && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            default -> false;
        };
    }
}