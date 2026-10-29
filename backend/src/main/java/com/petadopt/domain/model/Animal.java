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
        LocalDateTime fechaPublicacion,
        boolean vacunado,
        boolean esterilizado,
        TipoPublicacion tipo
) {
    public Animal conEstado(EstadoAnimal nuevoEstado) {
        return new Animal(id, nombre, especie, raza, edadMeses, sexo, descripcion,
                fotoUrl, nuevoEstado, ciudad, vendedorId, fechaPublicacion,
                vacunado, esterilizado, tipo);
    }

    public Animal conDatos(String nombre, EspecieAnimal especie, String raza, Integer edadMeses,
                           SexoAnimal sexo, String descripcion, String fotoUrl, String ciudad,
                           boolean vacunado, boolean esterilizado, TipoPublicacion tipo) {
        return new Animal(id, nombre, especie, raza, edadMeses, sexo, descripcion, fotoUrl,
                estado, ciudad, vendedorId, fechaPublicacion, vacunado, esterilizado, tipo);
    }
}
