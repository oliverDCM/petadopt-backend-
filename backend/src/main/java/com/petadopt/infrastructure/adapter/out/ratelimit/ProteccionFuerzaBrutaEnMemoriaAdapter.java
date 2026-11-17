package com.petadopt.infrastructure.adapter.out.ratelimit;

import com.petadopt.application.port.out.ProteccionFuerzaBrutaPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fallos de inicio de sesión por clave en una ventana deslizante, en memoria.
 * Sirve para una sola instancia del backend; con varias réplicas habría que usar Redis o la base de datos.
 * La memoria está acotada: cada clave guarda como máximo {@link #MAX_FALLOS_POR_CLAVE} marcas de tiempo y,
 * si se llega a {@link #MAX_CLAVES}, no se registran claves nuevas hasta la siguiente purga.
 */
@Component
public class ProteccionFuerzaBrutaEnMemoriaAdapter implements ProteccionFuerzaBrutaPort {

    static final int MAX_FALLOS_POR_CLAVE = 100;
    static final int MAX_CLAVES = 200_000;

    private final Map<String, Deque<Instant>> fallos = new ConcurrentHashMap<>();
    private final Duration ventana;
    private final Clock clock;

    @Autowired
    public ProteccionFuerzaBrutaEnMemoriaAdapter(@Value("${app.login.ventana-minutos:15}") long ventanaMinutos,
                                                 Clock clock) {
        this.ventana = Duration.ofMinutes(ventanaMinutos);
        this.clock = clock;
    }

    @Override
    public boolean superaLimite(String clave, int maximo) {
        Instant ahora = clock.instant();
        int[] vigentes = {0};
        fallos.computeIfPresent(clave, (k, lista) -> {
            descartarVencidos(lista, ahora);
            vigentes[0] = lista.size();
            return lista.isEmpty() ? null : lista;
        });
        return vigentes[0] >= maximo;
    }

    @Override
    public void registrarFallo(String clave) {
        if (fallos.size() >= MAX_CLAVES && !fallos.containsKey(clave)) {
            return;
        }
        Instant ahora = clock.instant();
        fallos.compute(clave, (k, registro) -> {
            Deque<Instant> lista = registro != null ? registro : new ArrayDeque<>();
            descartarVencidos(lista, ahora);
            if (lista.size() >= MAX_FALLOS_POR_CLAVE) {
                lista.pollFirst();
            }
            lista.addLast(ahora);
            return lista;
        });
    }

    @Override
    public void olvidar(String clave) {
        fallos.remove(clave);
    }

    /** Libera la memoria de las claves que ya no tienen fallos dentro de la ventana. */
    @Scheduled(fixedRateString = "${app.login.limpieza-ms:300000}")
    public void purgar() {
        Instant ahora = clock.instant();
        for (String clave : fallos.keySet()) {
            fallos.computeIfPresent(clave, (k, lista) -> {
                descartarVencidos(lista, ahora);
                return lista.isEmpty() ? null : lista;
            });
        }
    }

    int clavesRegistradas() {
        return fallos.size();
    }

    private void descartarVencidos(Deque<Instant> lista, Instant ahora) {
        Instant limite = ahora.minus(ventana);
        while (!lista.isEmpty() && !lista.peekFirst().isAfter(limite)) {
            lista.pollFirst();
        }
    }
}
