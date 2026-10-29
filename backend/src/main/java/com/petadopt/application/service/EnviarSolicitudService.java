package com.petadopt.application.service;

import com.petadopt.application.port.in.EnviarSolicitudUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.HistorialSolicitudRepositoryPort;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoDisponibleException;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.SoloAdoptantePuedeSolicitarException;
import com.petadopt.domain.exception.SolicitudPropioAnimalException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.HistorialSolicitud;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.petadopt.domain.exception.SolicitudDuplicadaException;

import java.time.LocalDateTime;

@Service
public class EnviarSolicitudService implements EnviarSolicitudUseCase {

    private final SolicitudRepositoryPort solicitudRepository;
    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final HistorialSolicitudRepositoryPort historialRepository;

    public EnviarSolicitudService(SolicitudRepositoryPort solicitudRepository,
                                  AnimalRepositoryPort animalRepository,
                                  UsuarioRepositoryPort usuarioRepository,
                                  HistorialSolicitudRepositoryPort historialRepository) {
        this.solicitudRepository = solicitudRepository;
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = historialRepository;
    }

    @Override
    @Transactional
    public Solicitud enviar(Comando comando) {
        Usuario solicitante = usuarioRepository.buscarPorEmail(comando.solicitanteEmail())
                .filter(u -> u.rol() == Rol.ADOPTANTE)
                .orElseThrow(SoloAdoptantePuedeSolicitarException::new);

        Animal animal = animalRepository.buscarPorIdParaActualizar(comando.animalId())
                .orElseThrow(() -> new AnimalNoEncontradoException(comando.animalId()));

        if (animal.vendedorId().equals(solicitante.id())) {
            throw new SolicitudPropioAnimalException();
        }
        if (animal.estado() != EstadoAnimal.DISPONIBLE) {
            throw new AnimalNoDisponibleException();
        }
        if (solicitudRepository.existeActivaPorAnimalYSolicitante(animal.id(), solicitante.id())) {
            throw new SolicitudDuplicadaException();
        }

        Solicitud nueva = new Solicitud(
                null,
                animal.id(),
                solicitante.id(),
                comando.mensaje().trim(),
                EstadoSolicitud.PENDIENTE,
                LocalDateTime.now(),
                null
        );
        Solicitud guardada = solicitudRepository.guardar(nueva);
        historialRepository.registrar(HistorialSolicitud.de(guardada.id(), null, EstadoSolicitud.PENDIENTE,
                solicitante.id(), guardada.fechaSolicitud()));
        return guardada;
    }

}