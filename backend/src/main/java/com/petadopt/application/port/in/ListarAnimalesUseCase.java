package com.petadopt.application.port.in;

import com.petadopt.domain.model.Animal;

import java.util.List;

public interface ListarAnimalesUseCase {

    List<Animal> listarDisponibles();
}