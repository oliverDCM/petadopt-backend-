package com.petadopt.application.service;

import com.petadopt.application.port.in.ConsultarAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarAnimalServiceTest {

    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @InjectMocks
    private ConsultarAnimalService service;

    private final Animal animal = new Animal(10L, "Firulais", EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.MACHO,
            "Cariñoso", null, EstadoAnimal.DISPONIBLE, "Fusagasugá", 1L, LocalDateTime.now(),
            true, false, TipoPublicacion.ADOPCION);

    @Test
    void devuelve_el_animal_con_nombre_y_ciudad_del_publicador() {
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.of(animal));
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(
                new Usuario(1L, "Carlos", "carlos@test.com", "h", "310", "Fusagasugá", Rol.VENDEDOR)));

        ConsultarAnimalUseCase.Detalle d = service.consultar(10L);

        assertThat(d.animal()).isEqualTo(animal);
        assertThat(d.vendedorNombre()).isEqualTo("Carlos");
        assertThat(d.vendedorCiudad()).isEqualTo("Fusagasugá");
    }

    @Test
    void animal_inexistente_devuelve_404() {
        when(animalRepository.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(AnimalNoEncontradoException.class, () -> service.consultar(99L));
    }
}
