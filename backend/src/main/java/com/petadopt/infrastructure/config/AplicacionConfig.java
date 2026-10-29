package com.petadopt.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/** Habilita tareas asíncronas (envío de correo) y programadas (limpieza de tokens). */
@Configuration
@EnableAsync
@EnableScheduling
public class AplicacionConfig {

    /** Reloj inyectable: en las pruebas se reemplaza por uno fijo. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
