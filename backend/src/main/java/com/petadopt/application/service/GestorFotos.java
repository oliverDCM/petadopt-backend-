package com.petadopt.application.service;

import com.petadopt.application.port.out.ImagenStoragePort;
import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.model.ImagenArchivo;

/** Reglas de las fotos de los animales, compartidas por publicar y editar. */
final class GestorFotos {

    private final ImagenStoragePort almacenamiento;

    GestorFotos(ImagenStoragePort almacenamiento) {
        this.almacenamiento = almacenamiento;
    }

    /** Valida el archivo, le quita los metadatos (GPS, modelo del teléfono) y lo sube. Devuelve null si no hay foto. */
    String subirSiHay(ImagenArchivo foto) {
        if (foto == null || foto.estaVacia()) {
            return null;
        }
        ValidacionImagen.validar(foto);
        return almacenamiento.subirImagen(LimpiadorMetadatos.limpiar(foto));
    }

    /**
     * La foto se sube como archivo. Un enlace solo se acepta si apunta a nuestro propio almacenamiento
     * o si es el que el animal ya tenía; así nadie puede poner en el catálogo imágenes de otros servidores
     * (sirven para rastrear a quien las ve).
     */
    String urlPermitida(String url, String urlActual) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (url.equals(urlActual) || almacenamiento.esUrlPropia(url)) {
            return url;
        }
        throw new ImagenInvalidaException("La foto debe subirse como archivo; no se aceptan enlaces externos");
    }

    /** Borra del almacenamiento una foto que dejó de usarse. No falla: es solo limpieza. */
    void descartar(String url) {
        if (url != null && almacenamiento.esUrlPropia(url)) {
            almacenamiento.eliminarImagen(url);
        }
    }
}
