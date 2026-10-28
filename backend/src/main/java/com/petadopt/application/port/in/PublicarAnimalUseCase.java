package com.petadopt.application.port.in;

import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.ImagenArchivo;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.TipoPublicacion;

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
            String ciudad,
            boolean vacunado,
            boolean esterilizado,
            TipoPublicacion tipo,
            ImagenArchivo foto
    ) {
    }
}