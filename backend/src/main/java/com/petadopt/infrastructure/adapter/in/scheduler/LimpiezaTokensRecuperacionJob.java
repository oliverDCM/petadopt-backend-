package com.petadopt.infrastructure.adapter.in.scheduler;

import com.petadopt.application.port.in.LimpiarTokensVencidosUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Borra cada hora los tokens de recuperación vencidos. */
@Component
public class LimpiezaTokensRecuperacionJob {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaTokensRecuperacionJob.class);

    private final LimpiarTokensVencidosUseCase limpiarTokens;

    public LimpiezaTokensRecuperacionJob(LimpiarTokensVencidosUseCase limpiarTokens) {
        this.limpiarTokens = limpiarTokens;
    }

    @Scheduled(cron = "${app.recuperacion.limpieza-cron:0 0 * * * *}")
    public void ejecutar() {
        int borrados = limpiarTokens.limpiar();
        if (borrados > 0) {
            log.info("Tokens de recuperación vencidos eliminados: {}", borrados);
        }
    }
}
