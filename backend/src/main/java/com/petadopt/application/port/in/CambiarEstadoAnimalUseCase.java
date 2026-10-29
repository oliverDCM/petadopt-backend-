package com.petadopt.application.port.in;

import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;

public interface CambiarEstadoAnimalUseCase {

    Animal cambiar(Comando comando);

    record Comando(String vendedorEmail, Long animalId, EstadoAnimal nuevoEstado) {
    }
}
