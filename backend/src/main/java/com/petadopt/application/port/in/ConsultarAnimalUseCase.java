package com.petadopt.application.port.in;

import com.petadopt.domain.model.Animal;

public interface ConsultarAnimalUseCase {

    Detalle consultar(Long animalId);

    /** El animal más los datos públicos de quien lo publicó (sin email ni teléfono). */
    record Detalle(Animal animal, String vendedorNombre, String vendedorCiudad) {
    }
}
