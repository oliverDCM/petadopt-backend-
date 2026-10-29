package com.petadopt.application.port.out;

/** Envío de correos transaccionales, sin depender de un proveedor concreto. */
public interface EmailSenderPort {

    void enviar(String destinatario, String asunto, String cuerpo);
}
