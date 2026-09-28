package com.petadopt.application.port.out;

public interface PasswordHasherPort {
    String cifrar(String passwordPlano);
}