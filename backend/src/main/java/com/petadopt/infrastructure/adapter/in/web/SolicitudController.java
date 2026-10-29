package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.DecidirSolicitudUseCase;
import com.petadopt.application.port.in.EnviarSolicitudUseCase;
import com.petadopt.application.port.in.ListarMisSolicitudesUseCase;
import com.petadopt.application.port.in.ListarSolicitudesRecibidasUseCase;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.infrastructure.adapter.in.web.docs.ApiEjemplos;
import com.petadopt.infrastructure.adapter.in.web.dto.DecisionRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.SolicitudRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.SolicitudResponse;
import com.petadopt.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Solicitudes", description = "Solicitudes de adopción o compra y su gestión")
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final EnviarSolicitudUseCase enviarSolicitud;
    private final DecidirSolicitudUseCase decidirSolicitud;
    private final ListarSolicitudesRecibidasUseCase listarRecibidas;
    private final ListarMisSolicitudesUseCase listarMias;

    public SolicitudController(EnviarSolicitudUseCase enviarSolicitud,
                               DecidirSolicitudUseCase decidirSolicitud,
                               ListarSolicitudesRecibidasUseCase listarRecibidas,
                               ListarMisSolicitudesUseCase listarMias) {
        this.enviarSolicitud = enviarSolicitud;
        this.decidirSolicitud = decidirSolicitud;
        this.listarRecibidas = listarRecibidas;
        this.listarMias = listarMias;
    }

    @Operation(summary = "Enviar una solicitud",
            description = "Solo para ADOPTANTE. El animal debe estar DISPONIBLE, no puede ser propio y no se permite "
                    + "una segunda solicitud activa sobre el mismo animal.",
            security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = ApiEjemplos.SOLICITUD_REQUEST)))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud registrada en estado PENDIENTE",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SolicitudResponse.class),
                            examples = @ExampleObject(value = ApiEjemplos.SOLICITUD_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o animal propio",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_VALIDACION))),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADOPTANTE"),
            @ApiResponse(responseCode = "404", description = "El animal no existe",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_NO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Animal no disponible o solicitud duplicada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_CONFLICTO)))
    })
    @PostMapping
    public ResponseEntity<SolicitudResponse> enviar(@Valid @RequestBody SolicitudRequest req,
                                                    Authentication authentication) {
        Solicitud solicitud = enviarSolicitud.enviar(new EnviarSolicitudUseCase.Comando(
                authentication.getName(), req.animalId(), req.mensaje()));
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitudResponse.desde(solicitud));
    }

    @Operation(summary = "Aprobar o rechazar una solicitud",
            description = "Solo el vendedor dueño del animal. Al aprobar, el animal pasa a RESERVADO y las demás "
                    + "solicitudes pendientes sobre él quedan RECHAZADAS.",
            security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = ApiEjemplos.DECISION_REQUEST)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud decidida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SolicitudResponse.class),
                            examples = @ExampleObject(value = ApiEjemplos.SOLICITUD_RESPONSE))),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no es el dueño del animal"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe"),
            @ApiResponse(responseCode = "409", description = "La solicitud ya fue decidida")
    })
    @PutMapping("/{id}/decision")
    public SolicitudResponse decidir(@PathVariable Long id,
                                     @Valid @RequestBody DecisionRequest req,
                                     Authentication authentication) {
        Solicitud solicitud = decidirSolicitud.decidir(new DecidirSolicitudUseCase.Comando(
                authentication.getName(), id, req.decision()));
        return SolicitudResponse.desde(solicitud);
    }

    @Operation(summary = "Solicitudes recibidas",
            description = "Solicitudes enviadas sobre los animales del vendedor autenticado.",
            security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitudes recibidas",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SolicitudResponse.class)),
                            examples = @ExampleObject(value = "[" + ApiEjemplos.SOLICITUD_RESPONSE + "]"))),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol VENDEDOR",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_ROL)))
    })
    @GetMapping("/recibidas")
    public List<SolicitudResponse> recibidas(Authentication authentication) {
        return listarRecibidas.listar(authentication.getName()).stream()
                .map(SolicitudResponse::desde)
                .toList();
    }

    @Operation(summary = "Seguimiento de mis solicitudes",
            description = "Solicitudes enviadas por el adoptante autenticado, con su estado.",
            security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitudes enviadas",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SolicitudResponse.class)),
                            examples = @ExampleObject(value = "[" + ApiEjemplos.SOLICITUD_RESPONSE + "]"))),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADOPTANTE")
    })
    @GetMapping("/mias")
    public List<SolicitudResponse> mias(Authentication authentication) {
        return listarMias.listar(authentication.getName()).stream()
                .map(SolicitudResponse::desde)
                .toList();
    }
}