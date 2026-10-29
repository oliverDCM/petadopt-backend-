package com.petadopt.domain.model;

import org.junit.jupiter.api.Test;

import static com.petadopt.domain.model.EstadoAnimal.ADOPTADO;
import static com.petadopt.domain.model.EstadoAnimal.DISPONIBLE;
import static com.petadopt.domain.model.EstadoAnimal.RESERVADO;
import static com.petadopt.domain.model.EstadoAnimal.RETIRADO;
import static org.assertj.core.api.Assertions.assertThat;

class EstadoAnimalTest {

    @Test
    void disponible_solo_puede_retirarse() {
        assertThat(DISPONIBLE.puedePasarA(RETIRADO)).isTrue();
        assertThat(DISPONIBLE.puedePasarA(ADOPTADO)).isFalse();
        assertThat(DISPONIBLE.puedePasarA(RESERVADO)).isFalse();
        assertThat(DISPONIBLE.puedePasarA(DISPONIBLE)).isFalse();
    }

    @Test
    void reservado_puede_adoptarse_liberarse_o_retirarse() {
        assertThat(RESERVADO.puedePasarA(ADOPTADO)).isTrue();
        assertThat(RESERVADO.puedePasarA(DISPONIBLE)).isTrue();
        assertThat(RESERVADO.puedePasarA(RETIRADO)).isTrue();
        assertThat(RESERVADO.puedePasarA(RESERVADO)).isFalse();
    }

    @Test
    void adoptado_y_retirado_son_finales() {
        for (EstadoAnimal destino : EstadoAnimal.values()) {
            assertThat(ADOPTADO.puedePasarA(destino)).isFalse();
            assertThat(RETIRADO.puedePasarA(destino)).isFalse();
        }
    }
}
