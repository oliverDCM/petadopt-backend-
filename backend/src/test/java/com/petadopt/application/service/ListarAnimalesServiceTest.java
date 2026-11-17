package com.petadopt.application.service;

import com.petadopt.application.port.in.ListarAnimalesUseCase;
import com.petadopt.application.port.in.ListarAnimalesUseCase.Filtro;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.Pagina;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El filtrado, el orden y la paginación los hace el repositorio; el servicio prepara y acota la consulta. */
@ExtendWith(MockitoExtension.class)
class ListarAnimalesServiceTest {

    @Mock
    private AnimalRepositoryPort animalRepository;
    @InjectMocks
    private ListarAnimalesService service;

    private final Animal lupe = new Animal(1L, "Lupe", EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.HEMBRA,
            null, null, EstadoAnimal.DISPONIBLE, "Fusagasugá", 9L, LocalDateTime.of(2026, 11, 1, 10, 0),
            true, false, TipoPublicacion.ADOPCION);

    @Test
    void pide_solo_disponibles_y_devuelve_la_pagina_del_repositorio() {
        Pagina<Animal> pagina = new Pagina<>(List.of(lupe), 1, 0, ListarAnimalesUseCase.TAMANO_POR_DEFECTO);
        when(animalRepository.buscar(EstadoAnimal.DISPONIBLE, null, null, null, 0,
                ListarAnimalesUseCase.TAMANO_POR_DEFECTO)).thenReturn(pagina);

        assertThat(service.listarDisponibles(Filtro.SIN_FILTRO)).isSameAs(pagina);
    }

    @Test
    void sin_filtro_se_comporta_como_la_primera_pagina_sin_filtros() {
        service.listarDisponibles(null);

        verify(animalRepository).buscar(EstadoAnimal.DISPONIBLE, null, null, null, 0,
                ListarAnimalesUseCase.TAMANO_POR_DEFECTO);
    }

    @Test
    void pasa_especie_y_tipo_y_normaliza_la_ciudad_sin_tildes_ni_mayusculas() {
        service.listarDisponibles(new Filtro(EspecieAnimal.GATO, TipoPublicacion.VENTA, "  FUSAGASUGÁ ", 2, 10));

        verify(animalRepository).buscar(EstadoAnimal.DISPONIBLE, EspecieAnimal.GATO, TipoPublicacion.VENTA,
                "fusagasuga", 2, 10);
    }

    @Test
    void una_ciudad_en_blanco_no_filtra() {
        service.listarDisponibles(new Filtro(null, null, "   ", 0, 10));

        verify(animalRepository).buscar(EstadoAnimal.DISPONIBLE, null, null, null, 0, 10);
    }

    @Test
    void acota_la_pagina_y_el_tamano_para_que_nadie_pida_la_tabla_entera() {
        service.listarDisponibles(new Filtro(null, null, null, -3, 5_000));
        verify(animalRepository).buscar(EstadoAnimal.DISPONIBLE, null, null, null, 0,
                ListarAnimalesUseCase.TAMANO_MAXIMO);

        service.listarDisponibles(new Filtro(null, null, null, 0, 0));
        verify(animalRepository).buscar(EstadoAnimal.DISPONIBLE, null, null, null, 0, 1);
    }

    @Test
    void normalizar_quita_tildes_dieresis_y_la_virgulilla() {
        assertThat(ListarAnimalesService.normalizar("Bogotá")).isEqualTo("bogota");
        assertThat(ListarAnimalesService.normalizar("  GÜICÁN ")).isEqualTo("guican");
        assertThat(ListarAnimalesService.normalizar("Nariño")).isEqualTo("narino");
    }
}
