package com.petadopt.infrastructure.adapter.in.web.dto;

import com.petadopt.domain.model.PerfilPublico;
import com.petadopt.domain.model.Rol;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PerfilPublicoResponseTest {

    @Test
    void el_dto_solo_expone_nombre_ciudad_y_rol() {
        List<String> campos = Arrays.stream(PerfilPublicoResponse.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        assertThat(campos).containsExactlyInAnyOrder("nombre", "ciudad", "rol");
        assertThat(campos).doesNotContain("password", "email", "telefono");
    }

    @Test
    void el_modelo_publico_tampoco_tiene_campos_sensibles() {
        List<String> campos = Arrays.stream(PerfilPublico.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        assertThat(campos).containsExactlyInAnyOrder("nombre", "ciudad", "rol");
    }

    @Test
    void desde_copia_los_tres_campos() {
        PerfilPublicoResponse respuesta =
                PerfilPublicoResponse.desde(new PerfilPublico("Ana Pérez", "Cali", Rol.ADOPTANTE));

        assertThat(respuesta.nombre()).isEqualTo("Ana Pérez");
        assertThat(respuesta.ciudad()).isEqualTo("Cali");
        assertThat(respuesta.rol()).isEqualTo(Rol.ADOPTANTE);
    }
}
