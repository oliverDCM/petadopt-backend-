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
    public Usuario conPassword(String nuevoHash) {
        return new Usuario(id, nombre, email, nuevoHash, telefono, ciudad, rol);
    }
}
