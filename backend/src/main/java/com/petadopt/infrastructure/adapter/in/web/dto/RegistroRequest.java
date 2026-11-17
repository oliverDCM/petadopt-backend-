package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(max = 100) @Pattern(regexp = "[^\\p{Cntrl}]*", message = "no puede contener saltos de línea ni caracteres de control")
        String nombre,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 20) @Pattern(regexp = "[^\\p{Cntrl}]*", message = "no puede contener saltos de línea ni caracteres de control")
        String telefono,
        @Size(max = 80) @Pattern(regexp = "[^\\p{Cntrl}]*", message = "no puede contener saltos de línea ni caracteres de control")
        String ciudad,
        @NotNull Rol rol
) {
}