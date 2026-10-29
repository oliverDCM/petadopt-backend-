package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.domain.exception.AlmacenamientoImagenException;
import com.petadopt.domain.exception.AnimalNoDisponibleException;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.ContactoNoDisponibleException;
import com.petadopt.domain.exception.ContrasenaInvalidaException;
import com.petadopt.domain.exception.CredencialesInvalidasException;
import com.petadopt.domain.exception.EmailYaRegistradoException;
import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.exception.LimiteSolicitudesExcedidoException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.exception.PerfilNoAccesibleException;
import com.petadopt.domain.exception.RolNoPermitidoException;
import com.petadopt.domain.exception.SolicitudDuplicadaException;
import com.petadopt.domain.exception.SolicitudNoEncontradaException;
import com.petadopt.domain.exception.SolicitudPropioAnimalException;
import com.petadopt.domain.exception.SolicitudYaDecididaException;
import com.petadopt.domain.exception.SoloAdoptantePuedeSolicitarException;
import com.petadopt.domain.exception.TokenRecuperacionInvalidoException;
import com.petadopt.domain.exception.TransicionEstadoInvalidaException;
import com.petadopt.domain.exception.UsuarioNoEncontradoException;
import com.petadopt.domain.exception.VendedorNoAutorizadoException;
import com.petadopt.infrastructure.adapter.in.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones a respuestas HTTP con un formato único (HU-BE-17):
 * {"codigo", "mensaje", "error", "timestamp", "ruta"} y "campos" en errores de validación.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 400: datos de la petición inválidos ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(ErrorResponse.deValidacion("Datos inválidos", req.getRequestURI(), campos));
    }

    @ExceptionHandler({SolicitudPropioAnimalException.class, ImagenInvalidaException.class,
            TokenRecuperacionInvalidoException.class, ContrasenaInvalidaException.class})
    public ResponseEntity<ErrorResponse> peticionInvalida(RuntimeException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido o está mal formado", req);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> parteFaltante(MissingServletRequestPartException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "Falta la parte obligatoria: " + ex.getRequestPartName(), req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "Valor inválido para el parámetro '" + ex.getName() + "'", req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> imagenMuyGrande(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "La imagen supera el tamaño máximo de 5 MB", req);
    }

    // ---------- 401 / 403 ----------

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credencialesInvalidas(CredencialesInvalidasException ex,
                                                               HttpServletRequest req) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler({VendedorNoAutorizadoException.class, SoloAdoptantePuedeSolicitarException.class,
            NoEsDuenoDelAnimalException.class, RolNoPermitidoException.class,
            PerfilNoAccesibleException.class, ContactoNoDisponibleException.class})
    public ResponseEntity<ErrorResponse> prohibido(RuntimeException ex, HttpServletRequest req) {
        return responder(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    // ---------- 404 ----------

    @ExceptionHandler({AnimalNoEncontradoException.class, SolicitudNoEncontradaException.class,
            UsuarioNoEncontradoException.class})
    public ResponseEntity<ErrorResponse> noEncontrado(RuntimeException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    // ---------- 409 ----------

    @ExceptionHandler({EmailYaRegistradoException.class, AnimalNoDisponibleException.class,
            SolicitudYaDecididaException.class, SolicitudDuplicadaException.class,
            TransicionEstadoInvalidaException.class})
    public ResponseEntity<ErrorResponse> conflicto(RuntimeException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridadDatos(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violación de integridad de datos en {}", req.getRequestURI(), ex);
        return responder(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes", req);
    }

    // ---------- 429 / 502 ----------

    @ExceptionHandler(LimiteSolicitudesExcedidoException.class)
    public ResponseEntity<ErrorResponse> demasiadasSolicitudes(LimiteSolicitudesExcedidoException ex,
                                                               HttpServletRequest req) {
        return responder(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    @ExceptionHandler(AlmacenamientoImagenException.class)
    public ResponseEntity<ErrorResponse> fallaAlmacenamiento(AlmacenamientoImagenException ex, HttpServletRequest req) {
        log.error("Fallo al subir la imagen a Cloudinary", ex);
        return responder(HttpStatus.BAD_GATEWAY, ex.getMessage(), req);
    }

    // ---------- Resto ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex, HttpServletRequest req) {
        if (ex instanceof org.springframework.web.ErrorResponse respuesta) {
            // errores propios de Spring (404 de ruta, 405, 415...) conservan su código
            String detalle = respuesta.getBody().getDetail();
            return responder(respuesta.getStatusCode(), detalle != null ? detalle : "Petición no válida", req);
        }
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req);
    }

    private static ResponseEntity<ErrorResponse> responder(HttpStatusCode estado, String mensaje,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(estado).body(ErrorResponse.de(estado.value(), mensaje, req.getRequestURI()));
    }
}
