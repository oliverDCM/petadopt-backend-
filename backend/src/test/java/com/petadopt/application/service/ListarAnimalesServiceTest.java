package com.petadopt.application.service;

import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.TipoPublicacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarAnimalesServiceTest {

    @Mock
    private AnimalRepositoryPort animalRepository;
    @InjectMocks
    private ListarAnimalesService service;

    @Test
    void devuelve_solo_los_animales_disponibles() {
        Animal disponible = new Animal(1L, "Firulais", EspecieAnimal.PERRO, "Criollo", 12,
                SexoAnimal.MACHO, "Cariñoso", null, EstadoAnimal.DISPONIBLE, "Bogotá", 1L,
                LocalDateTime.now(), true, true, TipoPublicacion.VENTA);
        when(animalRepository.listarPorEstado(EstadoAnimal.DISPONIBLE)).thenReturn(List.of(disponible));

        assertThat(service.listarDisponibles()).containsExactly(disponible);
    }

    @Test
    void devuelve_lista_vacia_si_no_hay_disponibles() {
        when(animalRepository.listarPorEstado(EstadoAnimal.DISPONIBLE)).thenReturn(List.of());

        assertThat(service.listarDisponibles()).isEmpty();
    }
}
