package com.petadopt.application.service;

import com.petadopt.application.port.in.ConsultarPerfilPublicoUseCase;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.PerfilNoAccesibleException;
import com.petadopt.domain.exception.RolNoPermitidoException;
import com.petadopt.domain.exception.UsuarioNoEncontradoException;
import com.petadopt.domain.model.PerfilPublico;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class ConsultarPerfilPublicoService implements ConsultarPerfilPublicoUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final SolicitudRepositoryPort solicitudRepository;

    public ConsultarPerfilPublicoService(UsuarioRepositoryPort usuarioRepository,
                                         SolicitudRepositoryPort solicitudRepository) {
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    public PerfilPublico consultar(String vendedorEmail, Long usuarioId) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(vendedorEmail)
                .filter(u -> u.rol() == Rol.VENDEDOR)
                .orElseThrow(() -> new RolNoPermitidoException(Rol.VENDEDOR));

        Usuario solicitante = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

        boolean envioSolicitud = solicitudRepository.listarPorVendedor(vendedor.id()).stream()
                .anyMatch(s -> s.solicitanteId().equals(solicitante.id()));
        if (!envioSolicitud) {
            throw new PerfilNoAccesibleException();
        }

        return new PerfilPublico(solicitante.nombre(), solicitante.ciudad(), solicitante.rol());
    }
}