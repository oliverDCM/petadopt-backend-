package com.petadopt.application.service;

import com.petadopt.application.port.in.ListarMisAnimalesUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.RolNoPermitidoException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarMisAnimalesService implements ListarMisAnimalesUseCase {

    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public ListarMisAnimalesService(AnimalRepositoryPort animalRepository,
                                    UsuarioRepositoryPort usuarioRepository) {
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Animal> listar(String vendedorEmail) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(vendedorEmail)
                .filter(u -> u.rol() == Rol.VENDEDOR)
                .orElseThrow(() -> new RolNoPermitidoException(Rol.VENDEDOR));
        return animalRepository.listarPorVendedor(vendedor.id());
    }
}