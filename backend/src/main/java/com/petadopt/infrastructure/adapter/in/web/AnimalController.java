package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.PublicarAnimalUseCase;
import com.petadopt.domain.model.Animal;
import com.petadopt.infrastructure.adapter.in.web.dto.AnimalRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.AnimalResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.petadopt.application.port.in.ListarAnimalesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/api/animales")
public class AnimalController {

    private final PublicarAnimalUseCase publicarAnimal;
    private final ListarAnimalesUseCase listarAnimales;

    public AnimalController(PublicarAnimalUseCase publicarAnimal,
                            ListarAnimalesUseCase listarAnimales) {
        this.publicarAnimal = publicarAnimal;
        this.listarAnimales = listarAnimales;
    }
    @GetMapping
    public List<AnimalResponse> listar() {
        return listarAnimales.listarDisponibles().stream()
                .map(AnimalResponse::desde)
                .toList();
    }
    @PostMapping
    public ResponseEntity<AnimalResponse> publicar(@Valid @RequestBody AnimalRequest req,
                                                   Authentication authentication) {
        Animal animal = publicarAnimal.publicar(new PublicarAnimalUseCase.Comando(
                authentication.getName(),
                req.nombre(), req.especie(), req.raza(), req.edadMeses(),
                req.sexo(), req.descripcion(), req.fotoUrl(), req.ciudad()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AnimalResponse.desde(animal));
    }
}