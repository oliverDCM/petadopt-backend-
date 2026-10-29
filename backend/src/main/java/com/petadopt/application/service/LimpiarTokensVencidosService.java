package com.petadopt.application.service;

import com.petadopt.application.port.in.LimpiarTokensVencidosUseCase;
import com.petadopt.application.port.out.TokenRecuperacionRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class LimpiarTokensVencidosService implements LimpiarTokensVencidosUseCase {

    private final TokenRecuperacionRepositoryPort tokenRepository;
    private final Clock clock;

    public LimpiarTokensVencidosService(TokenRecuperacionRepositoryPort tokenRepository, Clock clock) {
        this.tokenRepository = tokenRepository;
        this.clock = clock;
    }

    @Override
    public int limpiar() {
        return tokenRepository.eliminarVencidos(LocalDateTime.now(clock));
    }
}
