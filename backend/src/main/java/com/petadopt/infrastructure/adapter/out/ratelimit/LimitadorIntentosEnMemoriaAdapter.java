package com.petadopt.infrastructure.adapter.out.ratelimit;

import com.petadopt.application.port.out.LimitadorIntentosPort;
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
 * Ventana deslizante en memoria: como máximo N intentos por clave en la ventana configurada.
 * Sirve para una sola instancia del backend; con varias réplicas habría que usar Redis o la base de datos.
 */
@Component
public class LimitadorIntentosEnMemoriaAdapter implements LimitadorIntentosPort {

    private final Map<String, Deque<Instant>> intentos = new ConcurrentHashMap<>();
    private final int maximo;
    private final Duration ventana;
    private final Clock clock;

    @Autowired
    public LimitadorIntentosEnMemoriaAdapter(@Value("${app.recuperacion.max-intentos:3}") int maximo,
                                             @Value("${app.recuperacion.ventana-minutos:60}") long ventanaMinutos,
                                             Clock clock) {
        this.maximo = maximo;
        this.ventana = Duration.ofMinutes(ventanaMinutos);
        this.clock = clock;
    }

    @Override
    public boolean intentar(String clave) {
        Instant ahora = clock.instant();
        boolean[] permitido = {false};
        intentos.compute(clave, (k, registro) -> {
            Deque<Instant> lista = registro != null ? registro : new ArrayDeque<>();
            descartarVencidos(lista, ahora);
            if (lista.size() < maximo) {
                lista.addLast(ahora);
                permitido[0] = true;
            }
            return lista;
        });
        return permitido[0];
    }

    /** Libera la memoria de las claves que ya no tienen intentos dentro de la ventana. */
    @Scheduled(fixedRateString = "${app.recuperacion.limpieza-ms:600000}")
    public void purgar() {
        Instant ahora = clock.instant();
        for (String clave : intentos.keySet()) {
            // computeIfPresent es atómico por clave; devolver null elimina la entrada
            intentos.computeIfPresent(clave, (k, lista) -> {
                descartarVencidos(lista, ahora);
                return lista.isEmpty() ? null : lista;
            });
        }
    }

    int clavesRegistradas() {
        return intentos.size();
    }

    private void descartarVencidos(Deque<Instant> lista, Instant ahora) {
        Instant limite = ahora.minus(ventana);
        while (!lista.isEmpty() && !lista.peekFirst().isAfter(limite)) {
            lista.pollFirst();
        }
    }
}
