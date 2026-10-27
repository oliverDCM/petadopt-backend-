package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.application.port.in.DecidirSolicitudUseCase;
import jakarta.validation.constraints.NotNull;

public record DecisionRequest(@NotNull DecidirSolicitudUseCase.Decision decision) {
}