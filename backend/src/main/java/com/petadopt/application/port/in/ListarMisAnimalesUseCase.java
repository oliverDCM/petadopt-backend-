package com.petadopt.application.port.in;

import com.petadopt.domain.model.Animal;

import java.util.List;

public interface ListarMisAnimalesUseCase {

    List<Animal> listar(String vendedorEmail);
}