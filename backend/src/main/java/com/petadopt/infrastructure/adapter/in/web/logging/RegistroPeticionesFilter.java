package com.petadopt.infrastructure.adapter.in.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Estrategia de logs (HU-BE-19): una línea por petición con método, ruta, código y duración.
 * Cada petición recibe un identificador (cabecera X-Request-Id y campo requestId en el log)
 * para seguir todos los mensajes de una misma petición. Nunca se registran cuerpos,
 * cabeceras ni parámetros, porque pueden llevar contraseñas, tokens o datos de contacto.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RegistroPeticionesFilter extends OncePerRequestFilter {

    public static final String CABECERA_ID = "X-Request-Id";
    public static final String CLAVE_MDC = "requestId";

    private static final Logger log = LoggerFactory.getLogger(RegistroPeticionesFilter.class);
    private static final Set<String> RUTAS_SILENCIOSAS = Set.of("/api/health", "/actuator/health");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String id = UUID.randomUUID().toString().substring(0, 8);
        long inicio = System.nanoTime();
        MDC.put(CLAVE_MDC, id);
        response.setHeader(CABECERA_ID, id);
        try {
            chain.doFilter(request, response);
        } finally {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            registrar(request.getMethod(), request.getRequestURI(), response.getStatus(), ms);
            MDC.remove(CLAVE_MDC);
        }
    }

    private void registrar(String metodo, String ruta, int estado, long ms) {
        if (RUTAS_SILENCIOSAS.contains(ruta) && estado < 400) {
            return;
        }
        if (estado >= 500) {
            log.error("{} {} -> {} ({} ms)", metodo, ruta, estado, ms);
        } else if (estado >= 400) {
            log.warn("{} {} -> {} ({} ms)", metodo, ruta, estado, ms);
        } else {
            log.info("{} {} -> {} ({} ms)", metodo, ruta, estado, ms);
        }
    }
}
