package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.IniciarSesionUseCase;
import com.petadopt.application.port.in.RegistrarUsuarioUseCase;
import com.petadopt.domain.model.Usuario;
import com.petadopt.infrastructure.adapter.in.web.dto.LoginRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.LoginResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.RegistroRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest req) {
        Usuario usuario = registrarUsuario.registrar(new RegistrarUsuarioUseCase.Comando(
                req.nombre(), req.email(), req.password(),
                req.telefono(), req.ciudad(), req.rol()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(usuario));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        IniciarSesionUseCase.Resultado r = iniciarSesion.iniciarSesion(
                new IniciarSesionUseCase.Comando(req.email(), req.password()));
        return new LoginResponse(r.token(), r.rol(), r.nombre(), r.email());
    }
}
