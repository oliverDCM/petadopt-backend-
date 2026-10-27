package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.EnviarSolicitudUseCase;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.infrastructure.adapter.in.web.dto.SolicitudRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.SolicitudResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.petadopt.application.port.in.DecidirSolicitudUseCase;
import com.petadopt.infrastructure.adapter.in.web.dto.DecisionRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final EnviarSolicitudUseCase enviarSolicitud;
    private final DecidirSolicitudUseCase decidirSolicitud;

    public SolicitudController(EnviarSolicitudUseCase enviarSolicitud,
                               DecidirSolicitudUseCase decidirSolicitud) {
        this.enviarSolicitud = enviarSolicitud;
        this.decidirSolicitud = decidirSolicitud;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> enviar(@Valid @RequestBody SolicitudRequest req,
                                                    Authentication authentication) {
        Solicitud solicitud = enviarSolicitud.enviar(new EnviarSolicitudUseCase.Comando(
                authentication.getName(), req.animalId(), req.mensaje()));
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitudResponse.desde(solicitud));
    }
    @PutMapping("/{id}/decision")
    public SolicitudResponse decidir(@PathVariable Long id,
                                     @Valid @RequestBody DecisionRequest req,
                                     Authentication authentication) {
        Solicitud solicitud = decidirSolicitud.decidir(new DecidirSolicitudUseCase.Comando(
                authentication.getName(), id, req.decision()));
        return SolicitudResponse.desde(solicitud);
    }
}