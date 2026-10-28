package com.petadopt.domain.exception;

public class SolicitudDuplicadaException extends RuntimeException {
  public SolicitudDuplicadaException() {
    super("Ya tienes una solicitud activa sobre este animal");
  }
}