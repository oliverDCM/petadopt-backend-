package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.IniciarSesionUseCase;
import com.petadopt.application.port.in.RegistrarUsuarioUseCase;
import com.petadopt.application.port.in.RestablecerContrasenaUseCase;
import com.petadopt.application.port.in.SolicitarRecuperacionUseCase;
import com.petadopt.domain.model.Usuario;
import com.petadopt.infrastructure.adapter.in.web.docs.ApiEjemplos;
import com.petadopt.infrastructure.adapter.in.web.dto.LoginRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.LoginResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.MensajeResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.OlvideContrasenaRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.RegistroRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.RestablecerContrasenaRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación", description = "Registro, inicio de sesión y recuperación de contraseña")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuario;
    private final IniciarSesionUseCase iniciarSesion;
    private final SolicitarRecuperacionUseCase solicitarRecuperacion;
    private final RestablecerContrasenaUseCase restablecerContrasena;

    public AuthController(RegistrarUsuarioUseCase registrarUsuario,
                          IniciarSesionUseCase iniciarSesion,
                          SolicitarRecuperacionUseCase solicitarRecuperacion,
                          RestablecerContrasenaUseCase restablecerContrasena) {
        this.registrarUsuario = registrarUsuario;
        this.iniciarSesion = iniciarSesion;
        this.solicitarRecuperacion = solicitarRecuperacion;
        this.restablecerContrasena = restablecerContrasena;
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

    @Operation(summary = "Olvidé mi contraseña",
            description = "Público. Si el email está registrado, envía un enlace de recuperación válido 30 minutos. "
                    + "Responde siempre lo mismo para no revelar qué correos existen. "
                    + "Máximo 3 solicitudes por hora por email y por IP.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = ApiEjemplos.OLVIDE_REQUEST)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud recibida (exista o no la cuenta)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MensajeResponse.class),
                            examples = @ExampleObject(value = ApiEjemplos.OLVIDE_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "Email con formato inválido",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_VALIDACION))),
            @ApiResponse(responseCode = "429", description = "Se superó el límite de solicitudes",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_LIMITE)))
    })
    @PostMapping("/olvide-contrasena")
    public MensajeResponse olvideContrasena(@Valid @RequestBody OlvideContrasenaRequest req,
                                            HttpServletRequest request) {
        solicitarRecuperacion.solicitar(new SolicitarRecuperacionUseCase.Comando(req.email(), request.getRemoteAddr()));
        return new MensajeResponse("Si el correo está registrado, recibirás un enlace para restablecer tu contraseña");
    }

    @Operation(summary = "Restablecer contraseña",
            description = "Público. Recibe el token del enlace enviado por correo y la nueva contraseña (8 a 72 "
                    + "caracteres). El token solo sirve una vez y vence a los 30 minutos.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = ApiEjemplos.RESTABLECER_REQUEST)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MensajeResponse.class),
                            examples = @ExampleObject(value = ApiEjemplos.RESTABLECER_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "Token inválido, usado o vencido, o contraseña fuera de rango",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_TOKEN)))
    })
    @PostMapping("/restablecer-contrasena")
    public MensajeResponse restablecerContrasena(@Valid @RequestBody RestablecerContrasenaRequest req) {
        restablecerContrasena.restablecer(new RestablecerContrasenaUseCase.Comando(req.token(), req.nuevaPassword()));
        return new MensajeResponse("Tu contraseña se actualizó. Ya puedes iniciar sesión");
    }
}
