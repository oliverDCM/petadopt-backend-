package com.petadopt.application.port.in;

public interface SolicitarRecuperacionUseCase {

    /** No informa si el email existe: siempre termina igual para no revelar cuentas registradas. */
    void solicitar(Comando comando);

    record Comando(String email, String ip) {
    }
}
