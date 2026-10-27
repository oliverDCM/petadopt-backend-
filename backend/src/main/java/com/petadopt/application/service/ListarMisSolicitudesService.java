package com.petadopt.application.service;

import com.petadopt.application.port.in.ListarMisSolicitudesUseCase;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.RolNoPermitidoException;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarMisSolicitudesService implements ListarMisSolicitudesUseCase {

    private final SolicitudRepositoryPort solicitudRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public ListarMisSolicitudesService(SolicitudRepositoryPort solicitudRepository,
                                       UsuarioRepositoryPort usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Solicitud> listar(String solicitanteEmail) {
        Usuario solicitante = usuarioRepository.buscarPorEmail(solicitanteEmail)
                .filter(u -> u.rol() == Rol.ADOPTANTE)
                .orElseThrow(() -> new RolNoPermitidoException(Rol.ADOPTANTE));
        return solicitudRepository.listarPorSolicitante(solicitante.id());
    }
}