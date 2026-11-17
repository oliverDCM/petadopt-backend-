package com.petadopt.application.service;

/** Plantillas de texto de los correos transaccionales. */
final class PlantillasCorreo {

    record Correo(String asunto, String cuerpo) {
    }

    private static final String FIRMA = """

            Equipo PetAdopt
            (Este es un correo automático, por favor no lo respondas.)""";

    private PlantillasCorreo() {
    }

    static Correo solicitudAprobadaParaAdoptante(String nombreAdoptante, String nombreAnimal,
                                                 String nombreVendedor, String emailVendedor,
                                                 String telefonoVendedor, String ciudadVendedor) {
        String cuerpo = """
                Hola %s,

                ¡Buenas noticias! Tu solicitud por %s fue aprobada y el animal quedó reservado para ti.

                Ponte en contacto con quien lo publicó para acordar la entrega:
                  Nombre:   %s
                  Email:    %s
                  Teléfono: %s
                  Ciudad:   %s
                """.formatted(nombreAdoptante, nombreAnimal, nombreVendedor, emailVendedor,
                valor(telefonoVendedor), valor(ciudadVendedor));
        return new Correo("Tu solicitud por " + enUnaLinea(nombreAnimal) + " fue aprobada", cuerpo + FIRMA);
    }

    static Correo solicitudAprobadaParaVendedor(String nombreVendedor, String nombreAnimal,
                                                String nombreAdoptante, String emailAdoptante,
                                                String telefonoAdoptante, String ciudadAdoptante) {
        String cuerpo = """
                Hola %s,

                Aprobaste la solicitud por %s. El animal quedó en estado RESERVADO y las demás
                solicitudes pendientes se rechazaron automáticamente.

                Datos de contacto de la persona interesada:
                  Nombre:   %s
                  Email:    %s
                  Teléfono: %s
                  Ciudad:   %s

                Cuando se complete la entrega, marca el animal como ADOPTADO en "Mis publicaciones".
                """.formatted(nombreVendedor, nombreAnimal, nombreAdoptante, emailAdoptante,
                valor(telefonoAdoptante), valor(ciudadAdoptante));
        return new Correo("Aprobaste la solicitud por " + enUnaLinea(nombreAnimal), cuerpo + FIRMA);
    }

    static Correo solicitudCerrada(String nombreAdoptante, String nombreAnimal, boolean estabaAprobada) {
        String motivo = estabaAprobada
                ? "Quien publicó a %s canceló la reserva que tenías aprobada.".formatted(nombreAnimal)
                : "Quien publicó a %s retiró la publicación antes de responder tu solicitud.".formatted(nombreAnimal);
        String cuerpo = """
                Hola %s,

                %s
                Tu solicitud quedó cerrada y ya no se comparten datos de contacto.

                En el catálogo de PetAdopt hay más animales esperando un hogar.

                """.formatted(nombreAdoptante, motivo);
        return new Correo("Tu solicitud por " + enUnaLinea(nombreAnimal) + " fue cerrada", cuerpo + FIRMA);
    }

    static Correo recuperacionContrasena(String nombre, String enlace, long minutosVigencia) {
        String cuerpo = """
                Hola %s,

                Recibimos una solicitud para restablecer la contraseña de tu cuenta.
                Abre este enlace para elegir una nueva (vence en %d minutos y solo se puede usar una vez):

                %s

                Si no fuiste tú, ignora este correo: tu contraseña no cambiará.
                """.formatted(nombre, minutosVigencia, enlace);
        return new Correo("Restablece tu contraseña de PetAdopt", cuerpo + FIRMA);
    }

    static Correo contrasenaCambiada(String nombre) {
        String cuerpo = """
                Hola %s,

                La contraseña de tu cuenta de PetAdopt se cambió correctamente.

                Si no fuiste tú, solicita de inmediato una nueva recuperación de contraseña.
                """.formatted(nombre);
        return new Correo("Tu contraseña de PetAdopt fue cambiada", cuerpo + FIRMA);
    }

    /** Para el asunto: el texto que escribió un usuario no puede partir la cabecera ni la línea del log. */
    static String enUnaLinea(String texto) {
        return texto == null ? "" : texto.replaceAll("\\p{Cntrl}+", " ").trim();
    }

    private static String valor(String dato) {
        return dato == null || dato.isBlank() ? "(no registrado)" : dato;
    }
}
