package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.ConsultarPerfilPublicoUseCase;
import com.petadopt.infrastructure.adapter.in.web.docs.ApiEjemplos;
import com.petadopt.infrastructure.adapter.in.web.dto.PerfilPublicoResponse;
import com.petadopt.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuarios", description = "Consulta de perfiles públicos")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final ConsultarPerfilPublicoUseCase consultarPerfilPublico;

    public UsuarioController(ConsultarPerfilPublicoUseCase consultarPerfilPublico) {
        this.consultarPerfilPublico = consultarPerfilPublico;
    }

    @Operation(summary = "Perfil público de un solicitante",
            description = "Solo para VENDEDOR y únicamente de quien envió una solicitud sobre alguno de sus animales. "
                    + "Devuelve nombre, ciudad y rol; nunca email, teléfono ni contraseña.",
            security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil público",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PerfilPublicoResponse.class),
                            examples = @ExampleObject(value = ApiEjemplos.PERFIL_RESPONSE))),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "No es vendedor o el usuario no le envió solicitudes"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe")
    })
    @GetMapping("/{id}/perfil-publico")
    public PerfilPublicoResponse perfilPublico(@PathVariable Long id, Authentication authentication) {
        return PerfilPublicoResponse.desde(consultarPerfilPublico.consultar(authentication.getName(), id));
    }
}