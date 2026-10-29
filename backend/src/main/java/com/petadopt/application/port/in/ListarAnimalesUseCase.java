package com.petadopt.application.port.in;

import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.TipoPublicacion;

import java.util.List;

public interface ListarAnimalesUseCase {

    /** Catálogo público: solo animales DISPONIBLES que cumplen los filtros (los nulos no filtran). */
    List<Animal> listarDisponibles(Filtro filtro);

    default List<Animal> listarDisponibles() {
        return listarDisponibles(Filtro.SIN_FILTRO);
    }

    record Filtro(EspecieAnimal especie, TipoPublicacion tipo, String ciudad) {
        public static final Filtro SIN_FILTRO = new Filtro(null, null, null);
    }
}
