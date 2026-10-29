package com.petadopt.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
@Tag(name = "Salud", description = "Estado del servicio")
@RestController
@RequestMapping("/api")
public class HealthController {
    @Operation(summary = "Estado del servicio", description = "Público: responde UP si el backend está en línea.")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "petadopt-backend");
    }
}