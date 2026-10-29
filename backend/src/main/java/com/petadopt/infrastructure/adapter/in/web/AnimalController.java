package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.ListarAnimalesUseCase;
import com.petadopt.application.port.in.ListarMisAnimalesUseCase;
import com.petadopt.application.port.in.PublicarAnimalUseCase;
import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.ImagenArchivo;
import com.petadopt.infrastructure.adapter.in.web.docs.ApiEjemplos;
import com.petadopt.infrastructure.adapter.in.web.dto.AnimalRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.AnimalResponse;
import com.petadopt.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Animales", description = "Publicación y consulta de animales en adopción o venta")
@RestController
@RequestMapping("/api/animales")
public class AnimalController {

    private final PublicarAnimalUseCase publicarAnimal;
    private final ListarAnimalesUseCase listarAnimales;
    private final ListarMisAnimalesUseCase listarMisAnimales;

    public AnimalController(PublicarAnimalUseCase publicarAnimal,
                            ListarAnimalesUseCase listarAnimales,
                            ListarMisAnimalesUseCase listarMisAnimales) {
        this.publicarAnimal = publicarAnimal;
        this.listarAnimales = listarAnimales;
        this.listarMisAnimales = listarMisAnimales;
    }

    @Operation(summary = "Publicar un animal (sin foto)",
            description = "Solo para usuarios con rol VENDEDOR. Para adjuntar una foto usa la variante multipart.",
            security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = ApiEjemplos.ANIMAL_REQUEST)))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Animal publicado con estado DISPONIBLE",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnimalResponse.class),
                            examples = @ExampleObject(value = ApiEjemplos.ANIMAL_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_VALIDACION))),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol VENDEDOR")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnimalResponse> publicar(@Valid @RequestBody AnimalRequest req,
                                                   Authentication authentication) {
        return publicarConImagen(req, null, authentication);
    }

    @Operation(summary = "Publicar un animal con foto",
            description = "Formulario multipart: la parte 'datos' lleva el JSON del animal y la parte 'foto' la imagen "
                    + "(JPG, PNG o WEBP, máximo 5 MB). La imagen se sube a Cloudinary; si falla, el animal no se publica.",
            security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Animal publicado; fotoUrl apunta a Cloudinary",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnimalResponse.class),
                            examples = @ExampleObject(value = ApiEjemplos.ANIMAL_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, formato no permitido o imagen demasiado grande",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_IMAGEN))),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol VENDEDOR"),
            @ApiResponse(responseCode = "502", description = "Cloudinary no respondió; el animal no se publicó",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_ALMACENAMIENTO)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnimalResponse> publicarConFoto(
            @Valid @RequestPart("datos") AnimalRequest req,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            Authentication authentication) {
        return publicarConImagen(req, aImagen(foto), authentication);
    }

    @Operation(summary = "Consultar el catálogo",
            description = "Público: lista los animales con estado DISPONIBLE, sin necesidad de iniciar sesión.")
    @ApiResponse(responseCode = "200", description = "Animales disponibles",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AnimalResponse.class)),
                    examples = @ExampleObject(value = "[" + ApiEjemplos.ANIMAL_RESPONSE + "]")))
    @GetMapping
    public List<AnimalResponse> listar() {
        return listarAnimales.listarDisponibles().stream()
                .map(AnimalResponse::desde)
                .toList();
    }

    @Operation(summary = "Mis publicaciones",
            description = "Lista los animales publicados por el vendedor autenticado, con cualquier estado.",
            security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Animales del vendedor",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AnimalResponse.class)),
                            examples = @ExampleObject(value = "[" + ApiEjemplos.ANIMAL_RESPONSE + "]"))),
            @ApiResponse(responseCode = "401", description = "Falta el token"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol VENDEDOR",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ApiEjemplos.ERROR_ROL)))
    })
    @GetMapping("/mis-publicaciones")
    public List<AnimalResponse> misPublicaciones(Authentication authentication) {
        return listarMisAnimales.listar(authentication.getName()).stream()
                .map(AnimalResponse::desde)
                .toList();
    }

    private ResponseEntity<AnimalResponse> publicarConImagen(AnimalRequest req, ImagenArchivo foto,
                                                             Authentication authentication) {
        Animal animal = publicarAnimal.publicar(new PublicarAnimalUseCase.Comando(
                authentication.getName(),
                req.nombre(), req.especie(), req.raza(), req.edadMeses(),
                req.sexo(), req.descripcion(), req.fotoUrl(), req.ciudad(),
                req.vacunado(), req.esterilizado(), req.tipo(), foto));
        return ResponseEntity.status(HttpStatus.CREATED).body(AnimalResponse.desde(animal));
    }

    private ImagenArchivo aImagen(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            return null;
        }
        try {
            return new ImagenArchivo(foto.getOriginalFilename(), foto.getContentType(), foto.getBytes());
        } catch (IOException e) {
            throw new ImagenInvalidaException("No se pudo leer la imagen enviada");
        }
    }
}