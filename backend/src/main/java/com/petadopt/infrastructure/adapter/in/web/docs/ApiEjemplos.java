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

    public static final String PERFIL_RESPONSE = """
            {"nombre": "Ana Pérez", "ciudad": "Bogotá", "rol": "ADOPTANTE"}""";

    public static final String ERROR_VALIDACION = """
            {"error": "Datos inválidos", "campos": {"nombre": "no debe estar vacío"}}""";

    public static final String ERROR_ROL = """
            {"error": "Esta acción requiere el rol VENDEDOR"}""";

    public static final String ERROR_NO_ENCONTRADO = """
            {"error": "No existe un animal con id 99"}""";

    public static final String ERROR_CONFLICTO = """
            {"error": "El animal ya no está disponible"}""";

    public static final String ERROR_IMAGEN = """
            {"error": "Formato no permitido: use JPG, PNG o WEBP"}""";

    public static final String ERROR_ALMACENAMIENTO = """
            {"error": "No se pudo almacenar la imagen en el servicio externo"}""";
}