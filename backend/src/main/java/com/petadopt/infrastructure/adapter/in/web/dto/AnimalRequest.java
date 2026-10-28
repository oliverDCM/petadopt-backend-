package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.TipoPublicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AnimalRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull EspecieAnimal especie,
        @Size(max = 80) String raza,
        @PositiveOrZero Integer edadMeses,
        @NotNull SexoAnimal sexo,
        @Size(max = 1000) String descripcion,
        @Size(max = 500) String fotoUrl,
        @Size(max = 80) String ciudad,
        boolean vacunado,
        boolean esterilizado,
        @NotNull TipoPublicacion tipo
) {
}