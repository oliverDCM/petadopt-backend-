package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.ListarAnimalesUseCase;
import com.petadopt.application.port.in.ListarMisAnimalesUseCase;
import com.petadopt.application.port.in.PublicarAnimalUseCase;
import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.ImagenArchivo;
import com.petadopt.infrastructure.adapter.in.web.dto.AnimalRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.AnimalResponse;
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnimalResponse> publicar(@Valid @RequestBody AnimalRequest req,
                                                   Authentication authentication) {
        return publicarConImagen(req, null, authentication);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnimalResponse> publicarConFoto(
            @Valid @RequestPart("datos") AnimalRequest req,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            Authentication authentication) {
        return publicarConImagen(req, aImagen(foto), authentication);
    }

    @GetMapping
    public List<AnimalResponse> listar() {
        return listarAnimales.listarDisponibles().stream()
                .map(AnimalResponse::desde)
                .toList();
    }

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