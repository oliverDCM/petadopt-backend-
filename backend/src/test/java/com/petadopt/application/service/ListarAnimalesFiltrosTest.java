package com.petadopt.application.service;

import com.petadopt.application.port.in.ListarAnimalesUseCase.Filtro;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.TipoPublicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListarAnimalesFiltrosTest {

    private final AnimalRepositoryPort repo = mock(AnimalRepositoryPort.class);
    private final ListarAnimalesService service = new ListarAnimalesService(repo);

    private final Animal perroFusa = animal(1L, EspecieAnimal.PERRO, TipoPublicacion.ADOPCION, "Fusagasugá", 1);
    private final Animal gatoBogota = animal(2L, EspecieAnimal.GATO, TipoPublicacion.VENTA, "Bogotá", 3);
    private final Animal perroBogota = animal(3L, EspecieAnimal.PERRO, TipoPublicacion.VENTA, "bogota", 2);

    private static Animal animal(long id, EspecieAnimal especie, TipoPublicacion tipo, String ciudad, int dia) {
        return new Animal(id, "Animal " + id, especie, null, 12, SexoAnimal.MACHO, null, null,
                EstadoAnimal.DISPONIBLE, ciudad, 9L, LocalDateTime.of(2026, 10, dia, 10, 0), false, false, tipo);
    }

    @BeforeEach
    void setUp() {
        when(repo.listarPorEstado(EstadoAnimal.DISPONIBLE)).thenReturn(List.of(perroFusa, gatoBogota, perroBogota));
    }

    @Test
    void sin_filtros_devuelve_todos_del_mas_reciente_al_mas_antiguo() {
        assertThat(service.listarDisponibles(Filtro.SIN_FILTRO)).containsExactly(gatoBogota, perroBogota, perroFusa);
    }

    @Test
    void filtra_por_especie() {
        assertThat(service.listarDisponibles(new Filtro(EspecieAnimal.PERRO, null, null)))
                .containsExactly(perroBogota, perroFusa);
    }

    @Test
    void filtra_por_tipo() {
        assertThat(service.listarDisponibles(new Filtro(null, TipoPublicacion.ADOPCION, null))).containsExactly(perroFusa);
    }

    @Test
    void la_ciudad_ignora_mayusculas_y_tildes() {
        assertThat(service.listarDisponibles(new Filtro(null, null, "BOGOTÁ"))).containsExactly(gatoBogota, perroBogota);
        assertThat(service.listarDisponibles(new Filtro(null, null, "fusa"))).containsExactly(perroFusa);
    }

    @Test
    void combina_filtros() {
        assertThat(service.listarDisponibles(new Filtro(EspecieAnimal.PERRO, TipoPublicacion.VENTA, "bog")))
                .containsExactly(perroBogota);
    }
}
