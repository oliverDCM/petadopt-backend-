package com.petadopt.application.port.out;

import com.petadopt.domain.model.ImagenArchivo;

public interface ImagenStoragePort {

    String subirImagen(ImagenArchivo imagen);
}