package com.petadopt.infrastructure.adapter.in.web.docs;

public final class ApiEjemplos {

    private ApiEjemplos() {
    }

    public static final String REGISTRO_REQUEST = """
            {"nombre": "Ana Pérez", "email": "ana@test.com", "password": "12345678", "telefono": "3001234567", "ciudad": "Bogotá", "rol": "ADOPTANTE"}""";

    public static final String REGISTRO_RESPONSE = """
            {"id": 5, "nombre": "Ana Pérez", "email": "ana@test.com", "telefono": "3001234567", "ciudad": "Bogotá", "rol": "ADOPTANTE"}""";

    public static final String LOGIN_REQUEST = """
            {"email": "ana@test.com", "password": "12345678"}""";

    public static final String LOGIN_RESPONSE = """
            {"token": "eyJhbGciOiJIUzI1NiJ9...", "rol": "ADOPTANTE", "nombre": "Ana Pérez", "email": "ana@test.com"}""";

    public static final String ANIMAL_REQUEST = """
            {"nombre": "Firulais", "especie": "PERRO", "raza": "Criollo", "edadMeses": 12, "sexo": "MACHO", "descripcion": "Muy cariñoso", "ciudad": "Fusagasugá", "vacunado": true, "esterilizado": false, "tipo": "ADOPCION"}""";

    public static final String ANIMAL_RESPONSE = """
            {"id": 10, "nombre": "Firulais", "especie": "PERRO", "raza": "Criollo", "edadMeses": 12, "sexo": "MACHO", "descripcion": "Muy cariñoso", "fotoUrl": "https://res.cloudinary.com/demo/image/upload/v1/petadopt/animales/firulais.jpg", "estado": "DISPONIBLE", "ciudad": "Fusagasugá", "vendedorId": 2, "fechaPublicacion": "2026-10-28T14:30:00", "vacunado": true, "esterilizado": false, "tipo": "ADOPCION"}""";

    public static final String SOLICITUD_REQUEST = """
            {"animalId": 10, "mensaje": "Me encantaría adoptarlo, tengo patio y experiencia con perros"}""";

    public static final String SOLICITUD_RESPONSE = """
            {"id": 1, "animalId": 10, "solicitanteId": 5, "mensaje": "Me encantaría adoptarlo, tengo patio y experiencia con perros", "estado": "PENDIENTE", "fechaSolicitud": "2026-10-28T15:00:00", "fechaDecision": null}""";

    public static final String DECISION_REQUEST = """
            {"decision": "APROBAR"}""";

    public static final String ESTADO_REQUEST = """
            {"estado": "ADOPTADO"}""";

    public static final String ERROR_TRANSICION = """
            {"codigo": 409, "mensaje": "No se puede pasar un animal de RETIRADO a DISPONIBLE", "error": "No se puede pasar un animal de RETIRADO a DISPONIBLE", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/animales/10/estado"}""";

    public static final String OLVIDE_REQUEST = """
            {"email": "ana@test.com"}""";

    public static final String OLVIDE_RESPONSE = """
            {"mensaje": "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña"}""";

    public static final String RESTABLECER_REQUEST = """
            {"token": "Jx3b9tq0Yc2n8w4dA1fR7mZpLkQe5sVhUiOyTrEwBnM", "nuevaPassword": "nuevaClave123"}""";

    public static final String RESTABLECER_RESPONSE = """
            {"mensaje": "Tu contraseña se actualizó. Ya puedes iniciar sesión"}""";

    public static final String CONTACTO_RESPONSE = """
            {"nombre": "Carlos Gómez", "ciudad": "Fusagasugá", "email": "carlos@test.com", "telefono": "3109876543"}""";

    public static final String ERROR_TOKEN = """
            {"codigo": 400, "mensaje": "El enlace de recuperación venció; solicita uno nuevo", "error": "El enlace de recuperación venció; solicita uno nuevo", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/auth/restablecer-contrasena"}""";

    public static final String ERROR_LIMITE = """
            {"codigo": 429, "mensaje": "Demasiadas solicitudes de recuperación. Intenta de nuevo en una hora", "error": "Demasiadas solicitudes de recuperación. Intenta de nuevo en una hora", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/auth/olvide-contrasena"}""";

    public static final String ERROR_CONTACTO = """
            {"codigo": 403, "mensaje": "Los datos de contacto solo están disponibles para las partes de una solicitud aprobada", "error": "Los datos de contacto solo están disponibles para las partes de una solicitud aprobada", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/solicitudes/1/contacto"}""";

    public static final String PERFIL_RESPONSE = """
            {"nombre": "Ana Pérez", "ciudad": "Bogotá", "rol": "ADOPTANTE"}""";

    public static final String ERROR_VALIDACION = """
            {"codigo": 400, "mensaje": "Datos inválidos", "error": "Datos inválidos", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/animales", "campos": {"nombre": "no debe estar vacío"}}""";

    public static final String ERROR_ROL = """
            {"codigo": 403, "mensaje": "Esta acción requiere el rol VENDEDOR", "error": "Esta acción requiere el rol VENDEDOR", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/animales/mis-publicaciones"}""";

    public static final String ERROR_NO_ENCONTRADO = """
            {"codigo": 404, "mensaje": "No existe un animal con id 99", "error": "No existe un animal con id 99", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/animales/99"}""";

    public static final String ERROR_CONFLICTO = """
            {"codigo": 409, "mensaje": "El animal ya no está disponible", "error": "El animal ya no está disponible", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/solicitudes"}""";

    public static final String ERROR_IMAGEN = """
            {"codigo": 400, "mensaje": "Formato no permitido: use JPG, PNG o WEBP", "error": "Formato no permitido: use JPG, PNG o WEBP", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/animales/con-foto"}""";

    public static final String ERROR_ALMACENAMIENTO = """
            {"codigo": 502, "mensaje": "No se pudo almacenar la imagen en el servicio externo", "error": "No se pudo almacenar la imagen en el servicio externo", "timestamp": "2026-10-28T15:00:00-05:00", "ruta": "/api/animales/con-foto"}""";
}