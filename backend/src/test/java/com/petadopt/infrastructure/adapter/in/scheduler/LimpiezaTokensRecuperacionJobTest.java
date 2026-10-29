package com.petadopt.infrastructure.adapter.in.scheduler;

import com.petadopt.application.port.in.LimpiarTokensVencidosUseCase;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LimpiezaTokensRecuperacionJobTest {

    @Test
    void ejecuta_la_limpieza_aunque_no_haya_nada_que_borrar() {
        LimpiarTokensVencidosUseCase limpiar = mock(LimpiarTokensVencidosUseCase.class);
        when(limpiar.limpiar()).thenReturn(2, 0);
        LimpiezaTokensRecuperacionJob job = new LimpiezaTokensRecuperacionJob(limpiar);

        job.ejecutar();
        job.ejecutar();

        verify(limpiar, times(2)).limpiar();
    }
}
