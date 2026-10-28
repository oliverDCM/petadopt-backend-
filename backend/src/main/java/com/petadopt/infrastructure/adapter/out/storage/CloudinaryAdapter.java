package com.petadopt.infrastructure.adapter.out.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.petadopt.application.port.out.ImagenStoragePort;
import com.petadopt.domain.exception.AlmacenamientoImagenException;
import com.petadopt.domain.model.ImagenArchivo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CloudinaryAdapter implements ImagenStoragePort {

    private final Cloudinary cloudinary;
    private final String carpeta;

    public CloudinaryAdapter(Cloudinary cloudinary, @Value("${cloudinary.folder}") String carpeta) {
        this.cloudinary = cloudinary;
        this.carpeta = carpeta;
    }

    @Override
    public String subirImagen(ImagenArchivo imagen) {
        Map<?, ?> resultado;
        try {
            resultado = cloudinary.uploader().upload(imagen.getContenido(),
                    ObjectUtils.asMap("folder", carpeta, "resource_type", "image"));
        } catch (IOException | RuntimeException e) {
            throw new AlmacenamientoImagenException(e);
        }
        Object url = resultado.get("secure_url");
        if (url == null) {
            throw new AlmacenamientoImagenException(
                    new IllegalStateException("Cloudinary no devolvió la URL de la imagen"));
        }
        return url.toString();
    }
}