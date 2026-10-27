package com.petadopt.application.service;

import com.petadopt.application.port.in.DecidirSolicitudUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.exception.SolicitudNoEncontradaException;
import com.petadopt.domain.exception.SolicitudYaDecididaException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DecidirSolicitudService implements DecidirSolicitudUseCase {

    private final SolicitudRepositoryPort solicitudRepository;
    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public DecidirSolicitudService(SolicitudRepositoryPort solicitudRepository,
                                   AnimalRepositoryPort animalRepository,
                                   UsuarioRepositoryPort usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public Solicitud decidir(Comando comando) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(comando.vendedorEmail())
                .orElseThrow(NoEsDuenoDelAnimalException::new);

        Solicitud solicitud = solicitudRepository.buscarPorId(comando.solicitudId())
                .orElseThrow(() -> new SolicitudNoEncontradaException(comando.solicitudId()));

        Animal animal = animalRepository.buscarPorId(solicitud.animalId())
                .orElseThrow(() -> new AnimalNoEncontradoException(solicitud.animalId()));

        if (!animal.vendedorId().equals(vendedor.id())) {
            throw new NoEsDuenoDelAnimalException();
        }
        if (solicitud.estado() != EstadoSolicitud.PENDIENTE) {
            throw new SolicitudYaDecididaException();
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (comando.decision() == Decision.RECHAZAR) {
            return solicitudRepository.guardar(solicitud.conDecision(EstadoSolicitud.RECHAZADA, ahora));
        }

        Solicitud aprobada = solicitudRepository.guardar(
                solicitud.conDecision(EstadoSolicitud.APROBADA, ahora));
        animalRepository.guardar(animal.conEstado(EstadoAnimal.RESERVADO));

        solicitudRepository.listarPendientesPorAnimal(animal.id()).stream()
                .filter(otra -> !otra.id().equals(solicitud.id()))
                .forEach(otra -> solicitudRepository.guardar(
                        otra.conDecision(EstadoSolicitud.RECHAZADA, ahora)));

        return aprobada;
    }
}