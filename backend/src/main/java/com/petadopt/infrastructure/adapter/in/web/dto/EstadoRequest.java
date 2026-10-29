package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.EstadoAnimal;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull EstadoAnimal estado) {
}
