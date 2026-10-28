package com.petadopt.infrastructure.adapter.out.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.petadopt.domain.exception.AlmacenamientoImagenException;
import com.petadopt.domain.model.ImagenArchivo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryAdapterTest {

    private static final String URL_SEGURA = "https://res.cloudinary.com/demo/image/upload/v1/petadopt/firulais.jpg";

    @Mock
    private Cloudinary cloudinary;
    @Mock
    private Uploader uploader;

    private ImagenArchivo imagen() {
        return new ImagenArchivo("firulais.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    @SuppressWarnings("unchecked")
    void devuelve_la_url_segura_que_entrega_cloudinary() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), anyMap())).thenReturn(Map.of("secure_url", URL_SEGURA));
        CloudinaryAdapter adapter = new CloudinaryAdapter(cloudinary, "petadopt/animales");

        assertThat(adapter.subirImagen(imagen())).isEqualTo(URL_SEGURA);
    }

    @Test
    void traduce_un_fallo_de_red_en_un_error_de_almacenamiento() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), anyMap())).thenThrow(new IOException("sin red"));
        CloudinaryAdapter adapter = new CloudinaryAdapter(cloudinary, "petadopt/animales");

        ImagenArchivo imagen = imagen();
        assertThrows(AlmacenamientoImagenException.class, () -> adapter.subirImagen(imagen));
    }

    @Test
    @SuppressWarnings("unchecked")
    void falla_si_la_respuesta_no_trae_url() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), anyMap())).thenReturn(Map.of());
        CloudinaryAdapter adapter = new CloudinaryAdapter(cloudinary, "petadopt/animales");

        ImagenArchivo imagen = imagen();
        assertThrows(AlmacenamientoImagenException.class, () -> adapter.subirImagen(imagen));
    }
}
