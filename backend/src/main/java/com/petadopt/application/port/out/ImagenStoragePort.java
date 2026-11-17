package com.petadopt.application.port.out;

import com.petadopt.domain.model.ImagenArchivo;

public interface ImagenStoragePort {

    /** Guarda la imagen y devuelve la URL pública con la que se muestra. */
    String subirImagen(ImagenArchivo imagen);

    /** true si la URL apunta a una imagen guardada por este mismo almacenamiento. */
    boolean esUrlPropia(String url);

    /**
     * Borra una imagen propia que ya no se usa. Es una limpieza: si falla o la URL no es propia,
     * no lanza excepción (a lo sumo queda un archivo huérfano).
     */
    void eliminarImagen(String url);
}