package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.ConsultarPerfilPublicoUseCase;
import com.petadopt.infrastructure.adapter.in.web.dto.PerfilPublicoResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final ConsultarPerfilPublicoUseCase consultarPerfilPublico;

    public UsuarioController(ConsultarPerfilPublicoUseCase consultarPerfilPublico) {
        this.consultarPerfilPublico = consultarPerfilPublico;
    }

    @GetMapping("/{id}/perfil-publico")
    public PerfilPublicoResponse perfilPublico(@PathVariable Long id, Authentication authentication) {
        return PerfilPublicoResponse.desde(consultarPerfilPublico.consultar(authentication.getName(), id));
    }
}