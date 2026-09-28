package com.petadopt.application.port.out;

public interface PasswordHasherPort {
    String cifrar(String passwordPlano);

    boolean coincide(String passwordPlano, String hash);
}