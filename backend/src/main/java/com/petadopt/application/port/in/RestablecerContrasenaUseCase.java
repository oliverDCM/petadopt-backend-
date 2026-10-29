package com.petadopt.application.port.in;

public interface RestablecerContrasenaUseCase {

    void restablecer(Comando comando);

    record Comando(String token, String nuevaPassword) {
    }
}
