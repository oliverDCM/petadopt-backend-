package com.petadopt.application.service;

import com.petadopt.application.port.in.CambiarEstadoAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.exception.TransicionEstadoInvalidaException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CambiarEstadoAnimalServiceTest {

    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @InjectMocks
    private CambiarEstadoAnimalService service;

    private final Usuario vendedor = new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);

    private Animal animal(long vendedorId, EstadoAnimal estado) {
        return new Animal(10L, "Firulais", EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.MACHO,
                "Cariñoso", null, estado, "Bogotá", vendedorId, LocalDateTime.now(),
                false, false, TipoPublicacion.ADOPCION);
    }

    private CambiarEstadoAnimalUseCase.Comando comando(EstadoAnimal nuevo) {
        return new CambiarEstadoAnimalUseCase.Comando("v@test.com", 10L, nuevo);
    }

    @Test
    void el_dueno_marca_como_adoptado_un_animal_reservado() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.RESERVADO)));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal resultado = service.cambiar(comando(EstadoAnimal.ADOPTADO));

        assertThat(resultado.estado()).isEqualTo(EstadoAnimal.ADOPTADO);
        assertThat(resultado.nombre()).isEqualTo("Firulais");
    }

    @Test
    void el_dueno_retira_un_animal_disponible() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.DISPONIBLE)));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.cambiar(comando(EstadoAnimal.RETIRADO)).estado()).isEqualTo(EstadoAnimal.RETIRADO);
    }

    @Test
    void rechaza_una_transicion_no_permitida() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.ADOPTADO)));

        assertThrows(TransicionEstadoInvalidaException.class, () -> service.cambiar(comando(EstadoAnimal.DISPONIBLE)));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_a_quien_no_es_dueno() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(99L, EstadoAnimal.RESERVADO)));

        assertThrows(NoEsDuenoDelAnimalException.class, () -> service.cambiar(comando(EstadoAnimal.ADOPTADO)));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_usuario_inexistente() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.empty());

        assertThrows(NoEsDuenoDelAnimalException.class, () -> service.cambiar(comando(EstadoAnimal.ADOPTADO)));
    }

    @Test
    void rechaza_animal_inexistente() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.empty());

        assertThrows(AnimalNoEncontradoException.class, () -> service.cambiar(comando(EstadoAnimal.ADOPTADO)));
    }
}
