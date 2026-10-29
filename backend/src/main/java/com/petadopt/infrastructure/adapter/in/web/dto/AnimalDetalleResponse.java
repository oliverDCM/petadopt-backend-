package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.application.port.in.ConsultarAnimalUseCase;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.TipoPublicacion;

import java.time.LocalDateTime;

/** Detalle público de un animal: incluye nombre y ciudad del publicador, nunca su email ni teléfono. */
public record AnimalDetalleResponse(
        Long id,
        String nombre,
        EspecieAnimal especie,
        String raza,
        Integer edadMeses,
        SexoAnimal sexo,
        String descripcion,
        String fotoUrl,
        EstadoAnimal estado,
        String ciudad,
        Long vendedorId,
        LocalDateTime fechaPublicacion,
        boolean vacunado,
        boolean esterilizado,
        TipoPublicacion tipo,
        String vendedorNombre,
        String vendedorCiudad
) {
    public static AnimalDetalleResponse desde(ConsultarAnimalUseCase.Detalle d) {
        Animal a = d.animal();
        return new AnimalDetalleResponse(a.id(), a.nombre(), a.especie(), a.raza(), a.edadMeses(),
                a.sexo(), a.descripcion(), a.fotoUrl(), a.estado(), a.ciudad(), a.vendedorId(),
                a.fechaPublicacion(), a.vacunado(), a.esterilizado(), a.tipo(),
                d.vendedorNombre(), d.vendedorCiudad());
    }
}
