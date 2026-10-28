package com.petadopt.application.service;

import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.RolNoPermitidoException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.TipoPublicacion;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarMisAnimalesServiceTest {

    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @InjectMocks
    private ListarMisAnimalesService service;

    private Animal animal(long id, long vendedorId) {
        return new Animal(id, "Animal " + id, EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.MACHO,
                "Cariñoso", null, EstadoAnimal.DISPONIBLE, "Bogotá", vendedorId, LocalDateTime.now(),
                false, false, TipoPublicacion.ADOPCION);
    }

    @Test
    void el_vendedor_ve_solo_sus_propias_publicaciones() {
        Usuario vendedor = new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);
        Animal propio = animal(10L, 1L);
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.listarPorVendedor(1L)).thenReturn(List.of(propio));

        assertThat(service.listar("v@test.com")).containsExactly(propio);
        verify(animalRepository, never()).listarPorVendedor(2L);
    }

    @Test
    void rechaza_a_un_adoptante() {
        Usuario adoptante = new Usuario(2L, "Adoptante", "a@test.com", "hash", "300", "Bogotá", Rol.ADOPTANTE);
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(adoptante));

        assertThrows(RolNoPermitidoException.class, () -> service.listar("a@test.com"));
        verify(animalRepository, never()).listarPorVendedor(2L);
    }

    @Test
    void rechaza_a_un_usuario_inexistente() {
        when(usuarioRepository.buscarPorEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThrows(RolNoPermitidoException.class, () -> service.listar("nadie@test.com"));
    }
}
