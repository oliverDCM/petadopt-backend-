package com.petadopt.application.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/** Construye a mano imágenes pequeñas pero bien formadas, con y sin metadatos, para las pruebas. */
final class ImagenesDePrueba {

    static final String MARCA_GPS = "GPS-4.3369N-74.3639W";
    static final String MARCA_TELEFONO = "SM-A155M";

    private ImagenesDePrueba() {
    }

    // ---------------------------------------------------------------- JPEG

    /** JPEG mínimo sin metadatos: cabecera JFIF, una tabla, datos comprimidos y fin de imagen. */
    static byte[] jpeg() {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        escribir(s, 0xFF, 0xD8);
        segmento(s, 0xE0, concatenar("JFIF\0".getBytes(StandardCharsets.US_ASCII), new byte[]{1, 1, 0, 0, 1, 0, 1, 0, 0}));
        cuerpoJpeg(s);
        return s.toByteArray();
    }

    /** JPEG con EXIF (orientación, modelo del teléfono y "GPS"), XMP, un comentario y datos tras el fin de imagen. */
    static byte[] jpegConMetadatos(int orientacion) {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        escribir(s, 0xFF, 0xD8);
        segmento(s, 0xE1, exif(orientacion));
        segmento(s, 0xE1, ("http://ns.adobe.com/xap/1.0/\0<x:xmpmeta>" + MARCA_GPS + "</x:xmpmeta>")
                .getBytes(StandardCharsets.US_ASCII));
        segmento(s, 0xFE, ("comentario " + MARCA_TELEFONO).getBytes(StandardCharsets.US_ASCII));
        cuerpoJpeg(s);
        byte[] cola = ("oculto " + MARCA_GPS).getBytes(StandardCharsets.US_ASCII);
        s.write(cola, 0, cola.length);
        return s.toByteArray();
    }

    private static void cuerpoJpeg(ByteArrayOutputStream s) {
        segmento(s, 0xDB, new byte[65]); // tabla de cuantización
        segmento(s, 0xDA, new byte[]{1, 1, 0, 0, 63, 0}); // inicio de escaneo
        // datos comprimidos: incluyen un 0xFF de relleno (FF 00) y un marcador de reinicio (FF D0)
        escribir(s, 0x12, 0x34, 0xFF, 0x00, 0x56, 0xFF, 0xD0, 0x78);
        escribir(s, 0xFF, 0xD9);
    }

    /** Bloque EXIF en orden Intel con dos etiquetas: Model (0x0110) y Orientation (0x0112). */
    private static byte[] exif(int orientacion) {
        byte[] modelo = (MARCA_TELEFONO + " " + MARCA_GPS + "\0").getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream t = new ByteArrayOutputStream();
        escribir(t, 'E', 'x', 'i', 'f', 0, 0);
        escribir(t, 'I', 'I', 0x2A, 0, 8, 0, 0, 0); // cabecera TIFF, IFD0 en el desplazamiento 8
        escribir(t, 2, 0); // dos entradas
        int desplazamientoModelo = 8 + 2 + 2 * 12 + 4;
        escribir(t, 0x10, 0x01, 2, 0, modelo.length, 0, 0, 0, desplazamientoModelo, 0, 0, 0);
        escribir(t, 0x12, 0x01, 3, 0, 1, 0, 0, 0, orientacion, 0, 0, 0);
        escribir(t, 0, 0, 0, 0); // no hay más IFD
        t.write(modelo, 0, modelo.length);
        return t.toByteArray();
    }

    private static void segmento(ByteArrayOutputStream s, int marcador, byte[] datos) {
        int largo = datos.length + 2;
        escribir(s, 0xFF, marcador, largo >> 8, largo & 0xFF);
        s.write(datos, 0, datos.length);
    }

    // ---------------------------------------------------------------- PNG

    /** PNG de 1x1 sin metadatos. */
    static byte[] png() {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        cabeceraPng(s);
        bloquePng(s, "IDAT", new byte[]{0x78, (byte) 0x9C, 0x63, 0x60, 0x60, 0x60, 0, 0, 0, 4, 0, 1});
        bloquePng(s, "IEND", new byte[0]);
        return s.toByteArray();
    }

    /** PNG con texto, fecha y EXIF, y datos sobrantes después del final. */
    static byte[] pngConMetadatos() {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        cabeceraPng(s);
        bloquePng(s, "tEXt", ("Author\0" + MARCA_TELEFONO).getBytes(StandardCharsets.US_ASCII));
        bloquePng(s, "eXIf", MARCA_GPS.getBytes(StandardCharsets.US_ASCII));
        bloquePng(s, "tIME", new byte[]{7, (byte) 0xEA, 11, 16, 10, 0, 0});
        bloquePng(s, "IDAT", new byte[]{0x78, (byte) 0x9C, 0x63, 0x60, 0x60, 0x60, 0, 0, 0, 4, 0, 1});
        bloquePng(s, "IEND", new byte[0]);
        byte[] cola = ("oculto " + MARCA_GPS).getBytes(StandardCharsets.US_ASCII);
        s.write(cola, 0, cola.length);
        return s.toByteArray();
    }

    private static void cabeceraPng(ByteArrayOutputStream s) {
        escribir(s, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A);
        bloquePng(s, "IHDR", new byte[]{0, 0, 0, 1, 0, 0, 0, 1, 8, 2, 0, 0, 0});
    }

    private static void bloquePng(ByteArrayOutputStream s, String tipo, byte[] datos) {
        byte[] nombre = tipo.getBytes(StandardCharsets.US_ASCII);
        CRC32 crc = new CRC32();
        crc.update(nombre);
        crc.update(datos);
        long c = crc.getValue();
        escribir(s, datos.length >>> 24, datos.length >>> 16, datos.length >>> 8, datos.length);
        s.write(nombre, 0, 4);
        s.write(datos, 0, datos.length);
        escribir(s, (int) (c >>> 24), (int) (c >>> 16), (int) (c >>> 8), (int) c);
    }

    // ---------------------------------------------------------------- WEBP

    /** WEBP en formato extendido con un bloque EXIF y su bandera activada. */
    static byte[] webpConMetadatos() {
        ByteArrayOutputStream bloques = new ByteArrayOutputStream();
        bloqueWebp(bloques, "VP8X", new byte[]{0x08, 0, 0, 0, 0, 0, 0, 0, 0, 0}); // bandera EXIF
        bloqueWebp(bloques, "VP8 ", new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        bloqueWebp(bloques, "EXIF", MARCA_GPS.getBytes(StandardCharsets.US_ASCII));
        byte[] cuerpo = bloques.toByteArray();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        int tamano = cuerpo.length + 4;
        escribir(s, 'R', 'I', 'F', 'F', tamano, tamano >>> 8, tamano >>> 16, tamano >>> 24, 'W', 'E', 'B', 'P');
        s.write(cuerpo, 0, cuerpo.length);
        return s.toByteArray();
    }

    private static void bloqueWebp(ByteArrayOutputStream s, String tipo, byte[] datos) {
        byte[] nombre = tipo.getBytes(StandardCharsets.US_ASCII);
        s.write(nombre, 0, 4);
        escribir(s, datos.length, datos.length >>> 8, datos.length >>> 16, datos.length >>> 24);
        s.write(datos, 0, datos.length);
        if ((datos.length & 1) == 1) {
            s.write(0);
        }
    }

    // ---------------------------------------------------------------- utilidades

    static boolean contiene(byte[] datos, String texto) {
        return new String(datos, StandardCharsets.ISO_8859_1).contains(texto);
    }

    private static void escribir(ByteArrayOutputStream s, int... bytes) {
        for (int b : bytes) {
            s.write(b & 0xFF);
        }
    }

    private static byte[] concatenar(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
}
