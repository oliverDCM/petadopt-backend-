package com.petadopt.application.service;

import com.petadopt.application.port.in.ListarAnimalesUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ListarAnimalesService implements ListarAnimalesUseCase {

    private final AnimalRepositoryPort animalRepository;

    public ListarAnimalesService(AnimalRepositoryPort animalRepository) {
        this.animalRepository = animalRepository;
    }

    @Override
    public List<Animal> listarDisponibles(Filtro filtro) {
        Filtro f = filtro != null ? filtro : Filtro.SIN_FILTRO;
        String ciudad = f.ciudad() == null || f.ciudad().isBlank() ? null : normalizar(f.ciudad());
        return animalRepository.listarPorEstado(EstadoAnimal.DISPONIBLE).stream()
                .filter(a -> f.especie() == null || a.especie() == f.especie())
                .filter(a -> f.tipo() == null || a.tipo() == f.tipo())
                .filter(a -> ciudad == null || (a.ciudad() != null && normalizar(a.ciudad()).contains(ciudad)))
                .sorted(Comparator.comparing(Animal::fechaPublicacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** "Fusagasugá" y "fusagasuga" se consideran la misma ciudad. */
    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
