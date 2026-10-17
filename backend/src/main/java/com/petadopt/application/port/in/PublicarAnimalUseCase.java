package com.petadopt.application.port.in;

import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.SexoAnimal;

public interface PublicarAnimalUseCase {

    Animal publicar(Comando comando);

    record Comando(
            String vendedorEmail,
            String nombre,
            EspecieAnimal especie,
            String raza,
            Integer edadMeses,
            SexoAnimal sexo,
            String descripcion,
            String fotoUrl,
            String ciudad
    ) {
    }
}