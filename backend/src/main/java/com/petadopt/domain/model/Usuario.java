package com.petadopt.domain.model;

public record Usuario(
        Long id,
        String nombre,
        String email,
        String password,
        String telefono,
        String ciudad,
        Rol rol
) {
}