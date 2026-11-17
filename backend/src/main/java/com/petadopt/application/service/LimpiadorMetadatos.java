package com.petadopt.application.service;

import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.model.ImagenArchivo;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * Quita los metadatos de una foto antes de publicarla: las fotos de celular llevan en EXIF las coordenadas GPS
 * del lugar donde se tomaron (normalmente la casa de quien publica), el modelo del teléfono y la fecha.
 *
 * No recodifica la imagen: copia tal cual los datos de los píxeles y descarta solo los bloques de metadatos,
 * así no se pierde calidad. En JPEG conserva únicamente la orientación, para que la foto no quede girada.
 * De paso valida la estructura del archivo: si está truncado o mal formado, se rechaza.
 */
final class LimpiadorMetadatos {

    private static final String ARCHIVO_DANADO = "El archivo de imagen está dañado o incompleto";

    private LimpiadorMetadatos() {
    }

    static ImagenArchivo limpiar(ImagenArchivo foto) {
        String tipo = foto.getContentType() == null ? "" : foto.getContentType().toLowerCase(Locale.ROOT);
        byte[] original = foto.getContenido();
        byte[] limpio;
        try {
            limpio = switch (tipo) {
                case "image/jpeg" -> limpiarJpeg(original);
                case "image/png" -> limpiarPng(original);
                case "image/webp" -> limpiarWebp(original);
                default -> throw new ImagenInvalidaException("Formato no permitido: use JPG, PNG o WEBP");
            };
        } catch (IndexOutOfBoundsException | NegativeArraySizeException e) {
            throw new ImagenInvalidaException(ARCHIVO_DANADO);
        }
        return new ImagenArchivo(foto.getNombreOriginal(), foto.getContentType(), limpio);
    }

    // ------------------------------------------------------------------ JPEG

    private static final int SOI = 0xD8;
    private static final int EOI = 0xD9;
    private static final int SOS = 0xDA;
    private static final int APP0 = 0xE0;
    private static final int APP1 = 0xE1;
    private static final int APP2 = 0xE2;
    private static final int APP14 = 0xEE;
    private static final int APP15 = 0xEF;
    private static final int COM = 0xFE;
    private static final byte[] CABECERA_EXIF = {'E', 'x', 'i', 'f', 0, 0};
    private static final byte[] CABECERA_ICC = "ICC_PROFILE\0".getBytes(StandardCharsets.US_ASCII);

    private static byte[] limpiarJpeg(byte[] b) {
        if (b.length < 4 || u8(b, 0) != 0xFF || u8(b, 1) != SOI) {
            throw new ImagenInvalidaException(ARCHIVO_DANADO);
        }
        ByteArrayOutputStream cuerpo = new ByteArrayOutputStream(b.length);
        int orientacion = 1;
        int i = 2;
        while (true) {
            if (u8(b, i) != 0xFF) {
                throw new ImagenInvalidaException(ARCHIVO_DANADO);
            }
            while (u8(b, i + 1) == 0xFF) { // bytes de relleno antes del marcador
                i++;
            }
            int marcador = u8(b, i + 1);
            if (marcador == EOI) {
                cuerpo.write(0xFF);
                cuerpo.write(EOI);
                break; // lo que venga después del fin de imagen (videos, miniaturas, datos ocultos) se descarta
            }
            if (marcador == 0x01 || (marcador >= 0xD0 && marcador <= 0xD7)) { // marcadores sin longitud
                cuerpo.write(b, i, 2);
                i += 2;
                continue;
            }
            int largo = u16be(b, i + 2);
            if (largo < 2 || i + 2 + largo > b.length) {
                throw new ImagenInvalidaException(ARCHIVO_DANADO);
            }
            int datos = i + 4;
            int largoDatos = largo - 2;
            if (marcador == APP1 && empiezaCon(b, datos, largoDatos, CABECERA_EXIF)) {
                orientacion = leerOrientacion(b, datos + CABECERA_EXIF.length, largoDatos - CABECERA_EXIF.length);
            }
            if (seConserva(marcador, b, datos, largoDatos)) {
                cuerpo.write(b, i, 2 + largo);
            }
            i += 2 + largo;
            if (marcador == SOS) { // después vienen los píxeles comprimidos, hasta el siguiente marcador real
                int fin = finDeDatosComprimidos(b, i);
                cuerpo.write(b, i, fin - i);
                i = fin;
            }
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream(cuerpo.size() + 40);
        salida.write(0xFF);
        salida.write(SOI);
        if (orientacion >= 2 && orientacion <= 8) {
            byte[] exif = exifSoloOrientacion(orientacion);
            salida.write(exif, 0, exif.length);
        }
        byte[] resto = cuerpo.toByteArray();
        salida.write(resto, 0, resto.length);
        return salida.toByteArray();
    }

    /** Se descartan EXIF/XMP (APP1), IPTC (APP13), comentarios y el resto de bloques de aplicación. */
    private static boolean seConserva(int marcador, byte[] b, int datos, int largoDatos) {
        if (marcador == COM) {
            return false;
        }
        if (marcador >= APP0 && marcador <= APP15) {
            return marcador == APP0 // JFIF
                    || marcador == APP14 // Adobe: necesario para interpretar bien los colores
                    || (marcador == APP2 && empiezaCon(b, datos, largoDatos, CABECERA_ICC)); // perfil de color
        }
        return true; // tablas, cabeceras de imagen y de escaneo
    }

    private static int finDeDatosComprimidos(byte[] b, int desde) {
        int i = desde;
        while (true) {
            if (u8(b, i) == 0xFF) {
                int siguiente = u8(b, i + 1);
                boolean esDato = siguiente == 0x00 || (siguiente >= 0xD0 && siguiente <= 0xD7);
                if (!esDato && siguiente != 0xFF) {
                    return i;
                }
            }
            i++;
        }
    }

    /** Lee la etiqueta Orientation (0x0112) del bloque TIFF de EXIF. Ante cualquier anomalía devuelve 1 (normal). */
    private static int leerOrientacion(byte[] b, int tiff, int largo) {
        try {
            if (largo < 14) {
                return 1;
            }
            boolean intel;
            if (b[tiff] == 'I' && b[tiff + 1] == 'I') {
                intel = true;
            } else if (b[tiff] == 'M' && b[tiff + 1] == 'M') {
                intel = false;
            } else {
                return 1;
            }
            long ifd = u32(b, tiff + 4, intel);
            if (ifd < 8 || ifd + 2 > largo) {
                return 1;
            }
            int base = tiff + (int) ifd;
            int entradas = u16(b, base, intel);
            for (int n = 0; n < entradas; n++) {
                int e = base + 2 + n * 12;
                if (e + 12 > tiff + largo) {
                    return 1;
                }
                if (u16(b, e, intel) == 0x0112 && u16(b, e + 2, intel) == 3) {
                    int valor = u16(b, e + 8, intel);
                    return valor >= 1 && valor <= 8 ? valor : 1;
                }
            }
            return 1;
        } catch (IndexOutOfBoundsException e) {
            return 1;
        }
    }

    /** Bloque APP1 mínimo: un EXIF cuya única etiqueta es la orientación. */
    private static byte[] exifSoloOrientacion(int orientacion) {
        return new byte[]{
                (byte) 0xFF, (byte) APP1, 0x00, 0x22,
                'E', 'x', 'i', 'f', 0, 0,
                'M', 'M', 0x00, 0x2A, 0x00, 0x00, 0x00, 0x08,
                0x00, 0x01,
                0x01, 0x12, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, (byte) orientacion, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };
    }

    // ------------------------------------------------------------------ PNG

    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    /** Bloques necesarios para dibujar la imagen. Texto, fechas, EXIF y bloques desconocidos se descartan. */
    private static final Set<String> BLOQUES_PNG = Set.of(
            "IHDR", "PLTE", "IDAT", "IEND", "tRNS", "gAMA", "cHRM", "sRGB", "iCCP", "sBIT", "bKGD", "pHYs",
            "acTL", "fcTL", "fdAT", "cICP");

    private static byte[] limpiarPng(byte[] b) {
        if (!empiezaCon(b, 0, b.length, FIRMA_PNG)) {
            throw new ImagenInvalidaException(ARCHIVO_DANADO);
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream(b.length);
        salida.write(b, 0, FIRMA_PNG.length);
        int i = FIRMA_PNG.length;
        boolean primero = true;
        while (true) {
            long largo = u32(b, i, false);
            if (largo > b.length || i + 12 + largo > b.length) {
                throw new ImagenInvalidaException(ARCHIVO_DANADO);
            }
            String tipo = new String(b, i + 4, 4, StandardCharsets.US_ASCII);
            if (primero && !tipo.equals("IHDR")) {
                throw new ImagenInvalidaException(ARCHIVO_DANADO);
            }
            primero = false;
            int total = 12 + (int) largo;
            if (BLOQUES_PNG.contains(tipo)) {
                salida.write(b, i, total);
            }
            i += total;
            if (tipo.equals("IEND")) {
                return salida.toByteArray(); // lo que venga después se descarta
            }
        }
    }

    // ------------------------------------------------------------------ WEBP

    private static final Set<String> BLOQUES_WEBP = Set.of("VP8X", "ICCP", "ANIM", "ANMF", "ALPH", "VP8 ", "VP8L");
    private static final int BANDERAS_EXIF_Y_XMP = 0x08 | 0x04;

    private static byte[] limpiarWebp(byte[] b) {
        if (b.length < 20 || !ascii(b, 0).equals("RIFF") || !ascii(b, 8).equals("WEBP")) {
            throw new ImagenInvalidaException(ARCHIVO_DANADO);
        }
        long declarado = u32(b, 4, true) + 8;
        int fin = (int) Math.min(declarado, b.length);
        ByteArrayOutputStream bloques = new ByteArrayOutputStream(b.length);
        int i = 12;
        boolean hayImagen = false;
        while (i + 8 <= fin) {
            String tipo = ascii(b, i);
            long largo = u32(b, i + 4, true);
            long conRelleno = largo + (largo & 1);
            if (i + 8 + largo > b.length) {
                throw new ImagenInvalidaException(ARCHIVO_DANADO);
            }
            int total = (int) Math.min(8 + conRelleno, (long) b.length - i);
            if (BLOQUES_WEBP.contains(tipo)) {
                int inicio = bloques.size();
                bloques.write(b, i, total);
                if ((total & 1) == 1) {
                    bloques.write(0); // relleno a tamaño par, si el archivo original lo omitió al final
                }
                if (tipo.equals("VP8X") && largo >= 1) {
                    byte[] copia = bloques.toByteArray();
                    copia[inicio + 8] &= (byte) ~BANDERAS_EXIF_Y_XMP;
                    bloques.reset();
                    bloques.write(copia, 0, copia.length);
                }
                hayImagen |= tipo.equals("VP8 ") || tipo.equals("VP8L") || tipo.equals("ANMF");
            }
            i += total;
        }
        if (!hayImagen) {
            throw new ImagenInvalidaException(ARCHIVO_DANADO);
        }
        byte[] cuerpo = bloques.toByteArray();
        ByteArrayOutputStream salida = new ByteArrayOutputStream(cuerpo.length + 12);
        salida.write(b, 0, 4);
        int tamano = cuerpo.length + 4;
        salida.write(tamano & 0xFF);
        salida.write((tamano >>> 8) & 0xFF);
        salida.write((tamano >>> 16) & 0xFF);
        salida.write((tamano >>> 24) & 0xFF);
        salida.write(b, 8, 4);
        salida.write(cuerpo, 0, cuerpo.length);
        return salida.toByteArray();
    }

    // ------------------------------------------------------------------ utilidades

    private static int u8(byte[] b, int i) {
        return b[i] & 0xFF;
    }

    private static int u16be(byte[] b, int i) {
        return (u8(b, i) << 8) | u8(b, i + 1);
    }

    private static int u16(byte[] b, int i, boolean intel) {
        return intel ? (u8(b, i + 1) << 8) | u8(b, i) : u16be(b, i);
    }

    private static long u32(byte[] b, int i, boolean intel) {
        return intel
                ? ((long) u8(b, i + 3) << 24) | (u8(b, i + 2) << 16) | (u8(b, i + 1) << 8) | u8(b, i)
                : ((long) u8(b, i) << 24) | (u8(b, i + 1) << 16) | (u8(b, i + 2) << 8) | u8(b, i + 3);
    }

    private static String ascii(byte[] b, int i) {
        return new String(b, i, 4, StandardCharsets.US_ASCII);
    }

    private static boolean empiezaCon(byte[] b, int desde, int disponible, byte[] prefijo) {
        if (disponible < prefijo.length || desde + prefijo.length > b.length) {
            return false;
        }
        for (int n = 0; n < prefijo.length; n++) {
            if (b[desde + n] != prefijo[n]) {
                return false;
            }
        }
        return true;
    }
}
