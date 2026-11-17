package com.petadopt.application.service;

import com.petadopt.application.port.in.ListarAnimalesUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.Pagina;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Catálogo público. El filtrado, el orden y la paginación los hace la base de datos:
 * nunca se carga la tabla completa en memoria.
 */
@Service
public class ListarAnimalesService implements ListarAnimalesUseCase {

    private final AnimalRepositoryPort animalRepository;

    public ListarAnimalesService(AnimalRepositoryPort animalRepository) {
        this.animalRepository = animalRepository;
    }

    @Override
    public Pagina<Animal> listarDisponibles(Filtro filtro) {
        Filtro f = filtro != null ? filtro : Filtro.SIN_FILTRO;
        String ciudad = f.ciudad() == null || f.ciudad().isBlank() ? null : normalizar(f.ciudad());
        int pagina = Math.max(0, f.pagina());
        int tamano = Math.min(TAMANO_MAXIMO, Math.max(1, f.tamano()));
        return animalRepository.buscar(EstadoAnimal.DISPONIBLE, f.especie(), f.tipo(), ciudad, pagina, tamano);
    }

    /** "Fusagasugá" y "fusagasuga" se consideran la misma ciudad. */
    static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
