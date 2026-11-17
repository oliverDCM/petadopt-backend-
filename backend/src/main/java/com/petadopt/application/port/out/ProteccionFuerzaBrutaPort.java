package com.petadopt.application.port.out;

/** Cuenta los intentos fallidos por clave (IP, cuenta) para frenar la adivinación de contraseñas. */
public interface ProteccionFuerzaBrutaPort {

    /** true si la clave ya acumuló al menos {@code maximo} fallos dentro de la ventana de tiempo. */
    boolean superaLimite(String clave, int maximo);

    void registrarFallo(String clave);

    /** Borra los fallos de la clave (después de un inicio de sesión correcto). */
    void olvidar(String clave);
}
