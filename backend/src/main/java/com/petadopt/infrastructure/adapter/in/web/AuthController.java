package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.IniciarSesionUseCase;
import com.petadopt.application.port.in.RegistrarUsuarioUseCase;
import com.petadopt.domain.model.Usuario;
import com.petadopt.infrastructure.adapter.in.web.docs.ApiEjemplos;
import com.petadopt.infrastructure.adapter.in.web.dto.LoginRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.LoginResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.RegistroRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación", description = "Registro de usuarios e inicio de sesión")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuario;
    private final IniciarSesionUseCase iniciarSesion;

    public AuthController(RegistrarUsuarioUseCase registrarUsuario,
                          IniciarSesionUseCase iniciarSesion) {
        this.registrarUsuario = registrarUsuario;
        this.iniciarSesion = iniciarSesion;
    }

    @Operation(summary = "Registrar un usuario",
            description = "Crea una cuenta con rol VENDEDOR o ADOPTANTE. La contraseña se guarda cifrada con BCrypt.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = ApiEjemplos.REGISTRO_REQUEST)))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado (sin contraseña en la respuesta)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponse.class),
                            examples = @ExampleObject(value = ApiEjemplos.REGISTRO_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_VALIDACION))),
            @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese email")
    })
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest req) {
        Usuario usuario = registrarUsuario.registrar(new RegistrarUsuarioUseCase.Comando(
                req.nombre(), req.email(), req.password(),
                req.telefono(), req.ciudad(), req.rol()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(usuario));
    }

    @Operation(summary = "Iniciar sesión",
            description = "Devuelve un token JWT (válido 24 horas). Úsalo con el botón Authorize.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = ApiEjemplos.LOGIN_REQUEST)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión iniciada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = ApiEjemplos.LOGIN_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_VALIDACION))),
            @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos")
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        IniciarSesionUseCase.Resultado r = iniciarSesion.iniciarSesion(
                new IniciarSesionUseCase.Comando(req.email(), req.password()));
        return new LoginResponse(r.token(), r.rol(), r.nombre(), r.email());
    }
}