package com.petadopt.domain.model;

public enum EstadoAnimal {
    DISPONIBLE,
    RESERVADO,
    ADOPTADO,
    RETIRADO;

    /** Transiciones que el vendedor dueño puede hacer manualmente (RF-09). */
    public boolean puedePasarA(EstadoAnimal destino) {
        return switch (this) {
            case DISPONIBLE -> destino == RETIRADO;
            case RESERVADO -> destino == ADOPTADO || destino == DISPONIBLE || destino == RETIRADO;
            case ADOPTADO, RETIRADO -> false;
        };
    }
}
