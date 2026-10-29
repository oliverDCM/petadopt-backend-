package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.domain.exception.AnimalNoDisponibleException;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.ContactoNoDisponibleException;
import com.petadopt.domain.exception.CredencialesInvalidasException;
import com.petadopt.domain.exception.LimiteSolicitudesExcedidoException;
import com.petadopt.domain.exception.TokenRecuperacionInvalidoException;
import com.petadopt.domain.exception.TransicionEstadoInvalidaException;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.infrastructure.adapter.in.web.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/prueba");

    private static void verificarFormato(ResponseEntity<ErrorResponse> r, HttpStatus estado) {
        assertThat(r.getStatusCode()).isEqualTo(estado);
        ErrorResponse cuerpo = r.getBody();
        assertThat(cuerpo).isNotNull();
        assertThat(cuerpo.codigo()).isEqualTo(estado.value());
        assertThat(cuerpo.mensaje()).isNotBlank();
        assertThat(cuerpo.error()).isEqualTo(cuerpo.mensaje());
        assertThat(cuerpo.timestamp()).isNotNull();
        assertThat(cuerpo.ruta()).isEqualTo("/api/prueba");
    }

    @Test
    void no_encontrado_responde_404() {
        verificarFormato(handler.noEncontrado(new AnimalNoEncontradoException(9L), req), HttpStatus.NOT_FOUND);
    }

    @Test
    void conflictos_responden_409() {
        verificarFormato(handler.conflicto(new AnimalNoDisponibleException(), req), HttpStatus.CONFLICT);
        verificarFormato(handler.conflicto(
                new TransicionEstadoInvalidaException(EstadoAnimal.ADOPTADO, EstadoAnimal.DISPONIBLE), req),
                HttpStatus.CONFLICT);
    }

    @Test
    void prohibido_responde_403() {
        verificarFormato(handler.prohibido(new ContactoNoDisponibleException(), req), HttpStatus.FORBIDDEN);
    }

    @Test
    void credenciales_invalidas_responde_401() {
        verificarFormato(handler.credencialesInvalidas(new CredencialesInvalidasException(), req), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void token_invalido_responde_400() {
        verificarFormato(handler.peticionInvalida(new TokenRecuperacionInvalidoException("vencido"), req),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    void limite_excedido_responde_429() {
        verificarFormato(handler.demasiadasSolicitudes(new LimiteSolicitudesExcedidoException(), req),
                HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void error_inesperado_responde_500_sin_detalles_internos() {
        ResponseEntity<ErrorResponse> r = handler.inesperado(new IllegalStateException("password=secreto"), req);
        verificarFormato(r, HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(r.getBody().mensaje()).isEqualTo("Error interno del servidor");
    }
}
