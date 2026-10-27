package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.domain.exception.CredencialesInvalidasException;
import com.petadopt.domain.exception.EmailYaRegistradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.petadopt.domain.exception.VendedorNoAutorizadoException;
import com.petadopt.domain.exception.AnimalNoDisponibleException;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.SoloAdoptantePuedeSolicitarException;
import com.petadopt.domain.exception.SolicitudPropioAnimalException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.exception.SolicitudNoEncontradaException;
import com.petadopt.domain.exception.SolicitudYaDecididaException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<Map<String, String>> emailDuplicado(EmailYaRegistradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> credencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("error", "Datos inválidos", "campos", campos));
    }

    @ExceptionHandler(VendedorNoAutorizadoException.class)
    public ResponseEntity<Map<String, String>> vendedorNoAutorizado(VendedorNoAutorizadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(AnimalNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> animalNoEncontrado(AnimalNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AnimalNoDisponibleException.class)
    public ResponseEntity<Map<String, String>> animalNoDisponible(AnimalNoDisponibleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SoloAdoptantePuedeSolicitarException.class)
    public ResponseEntity<Map<String, String>> soloAdoptante(SoloAdoptantePuedeSolicitarException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SolicitudPropioAnimalException.class)
    public ResponseEntity<Map<String, String>> propioAnimal(SolicitudPropioAnimalException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> solicitudNoEncontrada(SolicitudNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SolicitudYaDecididaException.class)
    public ResponseEntity<Map<String, String>> yaDecidida(SolicitudYaDecididaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoEsDuenoDelAnimalException.class)
    public ResponseEntity<Map<String, String>> noEsDueno(NoEsDuenoDelAnimalException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
}
