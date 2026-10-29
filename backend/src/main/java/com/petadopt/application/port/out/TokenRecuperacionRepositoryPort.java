package com.petadopt.application.port.out;

import com.petadopt.domain.model.TokenRecuperacion;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacionRepositoryPort {

    TokenRecuperacion guardar(TokenRecuperacion token);

    /** Busca por hash y bloquea la fila hasta el fin de la transacción (evita usar el mismo token dos veces). */
    Optional<TokenRecuperacion> buscarPorHash(String tokenHash);

    /** Marca como usados todos los tokens sin usar del usuario. Devuelve cuántos anuló. */
    int anularVigentesDeUsuario(Long usuarioId);

    /** Borra los tokens cuya fecha de expiración ya pasó. Devuelve cuántos borró. */
    int eliminarVencidos(LocalDateTime ahora);
}
