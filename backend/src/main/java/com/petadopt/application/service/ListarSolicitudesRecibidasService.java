package com.petadopt.application.service;

import com.petadopt.application.port.in.ListarSolicitudesRecibidasUseCase;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.RolNoPermitidoException;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarSolicitudesRecibidasService implements ListarSolicitudesRecibidasUseCase {

    private final SolicitudRepositoryPort solicitudRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public ListarSolicitudesRecibidasService(SolicitudRepositoryPort solicitudRepository,
                                             UsuarioRepositoryPort usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Solicitud> listar(String vendedorEmail) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(vendedorEmail)
                .filter(u -> u.rol() == Rol.VENDEDOR)
                .orElseThrow(() -> new RolNoPermitidoException(Rol.VENDEDOR));
        return solicitudRepository.listarPorVendedor(vendedor.id());
    }
}