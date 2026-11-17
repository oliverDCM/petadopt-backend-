package com.petadopt.application.service;

import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.model.ImagenArchivo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LimpiadorMetadatosTest {

    private static byte[] limpiar(String tipo, byte[] contenido) {
        return LimpiadorMetadatos.limpiar(new ImagenArchivo("foto", tipo, contenido)).getContenido();
    }

    @Test
    void jpeg_pierde_gps_modelo_xmp_comentarios_y_lo_que_venga_despues_del_fin() {
        byte[] original = ImagenesDePrueba.jpegConMetadatos(1);
        assertThat(ImagenesDePrueba.contiene(original, ImagenesDePrueba.MARCA_GPS)).isTrue();

        byte[] limpio = limpiar("image/jpeg", original);

        assertThat(ImagenesDePrueba.contiene(limpio, ImagenesDePrueba.MARCA_GPS)).isFalse();
        assertThat(ImagenesDePrueba.contiene(limpio, ImagenesDePrueba.MARCA_TELEFONO)).isFalse();
        assertThat(ImagenesDePrueba.contiene(limpio, "xmpmeta")).isFalse();
        // sin metadatos queda exactamente la imagen: inicio + tablas + datos comprimidos + fin
        assertThat(limpio).isEqualTo(sinCabeceraJfif(ImagenesDePrueba.jpeg()));
    }

    @Test
    void jpeg_conserva_solo_la_orientacion_para_que_la_foto_no_quede_girada() {
        byte[] limpio = limpiar("image/jpeg", ImagenesDePrueba.jpegConMetadatos(6));

        assertThat(ImagenesDePrueba.contiene(limpio, ImagenesDePrueba.MARCA_GPS)).isFalse();
        assertThat(ImagenesDePrueba.contiene(limpio, "Exif")).isTrue();
        // bloque EXIF mínimo: FF E1, 34 bytes, y la orientación 6 como único dato
        assertThat(Arrays.copyOfRange(limpio, 2, 6)).containsExactly(0xFF, 0xE1, 0x00, 0x22);
        assertThat(limpio[2 + 4 + 6 + 8 + 2 + 9]).isEqualTo((byte) 6);
    }

    @Test
    void jpeg_sin_metadatos_no_cambia() {
        byte[] original = ImagenesDePrueba.jpeg();

        assertThat(limpiar("image/jpeg", original)).isEqualTo(original);
    }

    @Test
    void png_pierde_texto_fecha_exif_y_lo_que_venga_despues_del_fin() {
        byte[] limpio = limpiar("image/png", ImagenesDePrueba.pngConMetadatos());

        assertThat(limpio).isEqualTo(ImagenesDePrueba.png());
    }

    @Test
    void webp_pierde_el_bloque_exif_y_apaga_su_bandera() {
        byte[] limpio = limpiar("image/webp", ImagenesDePrueba.webpConMetadatos());

        assertThat(ImagenesDePrueba.contiene(limpio, ImagenesDePrueba.MARCA_GPS)).isFalse();
        assertThat(ImagenesDePrueba.contiene(limpio, "EXIF")).isFalse();
        assertThat(ImagenesDePrueba.contiene(limpio, "VP8 ")).isTrue();
        assertThat(limpio[20]).isEqualTo((byte) 0); // banderas de VP8X: ya no anuncia EXIF
        int declarado = (limpio[4] & 0xFF) | (limpio[5] & 0xFF) << 8 | (limpio[6] & 0xFF) << 16 | (limpio[7] & 0xFF) << 24;
        assertThat(declarado).isEqualTo(limpio.length - 8); // el tamaño de la cabecera se recalcula
    }

    @Test
    void limpiar_dos_veces_da_el_mismo_resultado() {
        byte[] una = limpiar("image/jpeg", ImagenesDePrueba.jpegConMetadatos(8));

        assertThat(limpiar("image/jpeg", una)).isEqualTo(una);
    }

    @Test
    void rechaza_archivos_truncados_o_mal_formados() {
        byte[] jpeg = ImagenesDePrueba.jpegConMetadatos(1);
        byte[] png = ImagenesDePrueba.png();
        byte[] webp = ImagenesDePrueba.webpConMetadatos();

        assertThatThrownBy(() -> limpiar("image/jpeg", Arrays.copyOf(jpeg, 40))).isInstanceOf(ImagenInvalidaException.class);
        assertThatThrownBy(() -> limpiar("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}))
                .isInstanceOf(ImagenInvalidaException.class);
        assertThatThrownBy(() -> limpiar("image/png", Arrays.copyOf(png, png.length - 5))).isInstanceOf(ImagenInvalidaException.class);
        assertThatThrownBy(() -> limpiar("image/webp", Arrays.copyOf(webp, 25))).isInstanceOf(ImagenInvalidaException.class);
    }

    @Test
    void rechaza_tipos_que_no_son_imagenes_permitidas() {
        assertThatThrownBy(() -> limpiar("image/gif", new byte[]{'G', 'I', 'F'})).isInstanceOf(ImagenInvalidaException.class);
        assertThatThrownBy(() -> limpiar(null, new byte[]{1})).isInstanceOf(ImagenInvalidaException.class);
    }

    /** jpeg() trae una cabecera JFIF que jpegConMetadatos() no tiene: se quita para comparar. */
    private static byte[] sinCabeceraJfif(byte[] jpeg) {
        int largoSegmento = 2 + ((jpeg[4] & 0xFF) << 8 | (jpeg[5] & 0xFF));
        byte[] r = new byte[jpeg.length - largoSegmento];
        System.arraycopy(jpeg, 0, r, 0, 2);
        System.arraycopy(jpeg, 2 + largoSegmento, r, 2, jpeg.length - 2 - largoSegmento);
        return r;
    }
}
