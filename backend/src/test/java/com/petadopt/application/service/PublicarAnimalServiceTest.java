package com.petadopt.application.service;

import com.petadopt.application.port.in.PublicarAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.VendedorNoAutorizadoException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicarAnimalServiceTest {

    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @InjectMocks
    private PublicarAnimalService service;

    private PublicarAnimalUseCase.Comando comando(String email) {
        return new PublicarAnimalUseCase.Comando(email, "  Firulais  ", EspecieAnimal.PERRO,
                "Criollo", 12, SexoAnimal.MACHO, "Muy cariñoso", null, "Fusagasugá");
    }

    @Test
    void publica_animal_disponible_asociado_al_vendedor() {
        Usuario vendedor = new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal resultado = service.publicar(comando("v@test.com"));

        assertThat(resultado.nombre()).isEqualTo("Firulais");
        assertThat(resultado.estado()).isEqualTo(EstadoAnimal.DISPONIBLE);
        assertThat(resultado.vendedorId()).isEqualTo(1L);
    }

    @Test
    void rechaza_usuario_que_no_es_vendedor() {
        Usuario adoptante = new Usuario(2L, "Adoptante", "a@test.com", "hash", "300", "Bogotá", Rol.ADOPTANTE);
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(adoptante));

        assertThrows(VendedorNoAutorizadoException.class, () -> service.publicar(comando("a@test.com")));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_usuario_inexistente() {
        when(usuarioRepository.buscarPorEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThrows(VendedorNoAutorizadoException.class, () -> service.publicar(comando("nadie@test.com")));
        verify(animalRepository, never()).guardar(any());
    }
}