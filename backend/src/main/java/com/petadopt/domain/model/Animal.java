package com.petadopt.domain.model;

import java.time.LocalDateTime;

public record Animal(
        Long id,
        String nombre,
        EspecieAnimal especie,
        String raza,
        Integer edadMeses,
        SexoAnimal sexo,
        String descripcion,
        String fotoUrl,
        EstadoAnimal estado,
        String ciudad,
        Long vendedorId,
        LocalDateTime fechaPublicacion
) {
}