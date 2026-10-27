package com.petadopt.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitudRequest(
        @NotNull Long animalId,
        @NotBlank @Size(max = 500) String mensaje
) {
}