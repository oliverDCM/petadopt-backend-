package com.petadopt.domain.model;

public final class ImagenArchivo {

    private final String nombreOriginal;
    private final String contentType;
    private final byte[] contenido;

    public ImagenArchivo(String nombreOriginal, String contentType, byte[] contenido) {
        this.nombreOriginal = nombreOriginal;
        this.contentType = contentType;
        this.contenido = contenido;
    }

    public String getNombreOriginal() { return nombreOriginal; }
    public String getContentType() { return contentType; }
    public byte[] getContenido() { return contenido; }

    public long tamanoBytes() {
        return contenido.length;
    }

    public boolean estaVacia() {
        return contenido.length == 0;
    }
}