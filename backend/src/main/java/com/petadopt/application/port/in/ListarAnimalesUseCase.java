package com.petadopt.application.port.in;

import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.Pagina;
import com.petadopt.domain.model.TipoPublicacion;

public interface ListarAnimalesUseCase {

    int TAMANO_POR_DEFECTO = 24;
    int TAMANO_MAXIMO = 100;

    /** Catálogo público paginado: solo animales DISPONIBLES que cumplen los filtros (los nulos no filtran). */
    Pagina<Animal> listarDisponibles(Filtro filtro);

    record Filtro(EspecieAnimal especie, TipoPublicacion tipo, String ciudad, int pagina, int tamano) {
        public static final Filtro SIN_FILTRO = new Filtro(null, null, null);

        /** Primera página con el tamaño por defecto. */
        public Filtro(EspecieAnimal especie, TipoPublicacion tipo, String ciudad) {
            this(especie, tipo, ciudad, 0, TAMANO_POR_DEFECTO);
        }
    }
}
