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
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

/**
 * La foto se sube ANTES de abrir la transacción: una subida lenta al almacenamiento externo no puede
 * retener una conexión de la base de datos ni la fila del animal bloqueada.
 */
@Service
public class EditarAnimalService implements EditarAnimalUseCase {

    private static final String SOLO_DUENO = "Solo el vendedor dueño puede editar el animal";

    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final TransactionOperations transaccion;
    private final GestorFotos fotos;

    public EditarAnimalService(AnimalRepositoryPort animalRepository,
                               UsuarioRepositoryPort usuarioRepository,
                               ImagenStoragePort imagenStorage,
                               TransactionOperations transaccion) {
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
        this.transaccion = transaccion;
        this.fotos = new GestorFotos(imagenStorage);
    }

    @Override
    public Animal editar(Comando c) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(c.vendedorEmail())
                .orElseThrow(() -> new NoEsDuenoDelAnimalException(SOLO_DUENO));

        // 1. Comprobación rápida, sin bloqueo: quien no puede editar no llega a subir nada.
        Animal actual = animalRepository.buscarPorId(c.animalId())
                .orElseThrow(() -> new AnimalNoEncontradoException(c.animalId()));
        validarEditable(actual, vendedor);
        String fotoAnterior = actual.fotoUrl();
        String enlace = fotos.urlPermitida(c.fotoUrl(), fotoAnterior);

        // 2. Subida al almacenamiento externo, fuera de toda transacción.
        String subida = fotos.subirSiHay(c.foto());

        // 3. Transacción corta: se bloquea la fila, se vuelve a validar con el dato actual y se guarda.
        Animal guardado;
        try {
            guardado = transaccion.execute(estado -> {
                Animal bloqueado = animalRepository.buscarPorIdParaActualizar(c.animalId())
                        .orElseThrow(() -> new AnimalNoEncontradoException(c.animalId()));
                validarEditable(bloqueado, vendedor);
                String fotoUrl = subida != null ? subida : (enlace != null ? enlace : bloqueado.fotoUrl());
                return animalRepository.guardar(bloqueado.conDatos(
                        c.nombre().trim(), c.especie(), c.raza(), c.edadMeses(), c.sexo(),
                        c.descripcion(), fotoUrl, c.ciudad(), c.vacunado(), c.esterilizado(), c.tipo()));
            });
        } catch (RuntimeException e) {
            fotos.descartar(subida); // no se guardó el cambio: la foto recién subida quedaría huérfana
            throw e;
        }

        // 4. La foto reemplazada ya no se usa.
        if (fotoAnterior != null && !fotoAnterior.equals(guardado.fotoUrl())) {
            fotos.descartar(fotoAnterior);
        }
        return guardado;
    }

    private static void validarEditable(Animal animal, Usuario vendedor) {
        if (!animal.vendedorId().equals(vendedor.id())) {
            throw new NoEsDuenoDelAnimalException(SOLO_DUENO);
        }
        if (animal.estado() != EstadoAnimal.DISPONIBLE) {
            throw new AnimalNoDisponibleException();
        }
    }
}
