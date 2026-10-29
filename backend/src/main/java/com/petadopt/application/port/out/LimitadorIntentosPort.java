package com.petadopt.application.port.out;

/** Controla cuántas veces se puede repetir una acción por clave (email, IP...) en una ventana de tiempo. */
public interface LimitadorIntentosPort {

    /** Registra un intento. Devuelve false si la clave ya alcanzó el máximo y el intento no se permite. */
    boolean intentar(String clave);
}
