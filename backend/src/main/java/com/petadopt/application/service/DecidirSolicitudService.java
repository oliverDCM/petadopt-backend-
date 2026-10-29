package com.petadopt.application.service;

import com.petadopt.application.port.in.DecidirSolicitudUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.EmailSenderPort;
import com.petadopt.application.port.out.HistorialSolicitudRepositoryPort;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoDisponibleException;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.exception.SolicitudNoEncontradaException;
import com.petadopt.domain.exception.SolicitudYaDecididaException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.HistorialSolicitud;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DecidirSolicitudService implements DecidirSolicitudUseCase {

    private static final Logger log = LoggerFactory.getLogger(DecidirSolicitudService.class);

    private final SolicitudRepositoryPort solicitudRepository;
    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final HistorialSolicitudRepositoryPort historialRepository;
    private final EmailSenderPort emailSender;

    public DecidirSolicitudService(SolicitudRepositoryPort solicitudRepository,
                                   AnimalRepositoryPort animalRepository,
                                   UsuarioRepositoryPort usuarioRepository,
                                   HistorialSolicitudRepositoryPort historialRepository,
                                   EmailSenderPort emailSender) {
        this.solicitudRepository = solicitudRepository;
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = historialRepository;
        this.emailSender = emailSender;
    }

    @Override
    @Transactional
    public Solicitud decidir(Comando comando) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(comando.vendedorEmail())
                .orElseThrow(NoEsDuenoDelAnimalException::new);

        Solicitud solicitud = solicitudRepository.buscarPorId(comando.solicitudId())
                .orElseThrow(() -> new SolicitudNoEncontradaException(comando.solicitudId()));

        Animal animal = animalRepository.buscarPorIdParaActualizar(solicitud.animalId())
                .orElseThrow(() -> new AnimalNoEncontradoException(solicitud.animalId()));

        if (!animal.vendedorId().equals(vendedor.id())) {
            throw new NoEsDuenoDelAnimalException();
        }
        if (solicitud.estado() != EstadoSolicitud.PENDIENTE) {
            throw new SolicitudYaDecididaException();
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (comando.decision() == Decision.RECHAZAR) {
            return cambiarEstado(solicitud, EstadoSolicitud.RECHAZADA, vendedor.id(), ahora);
        }
        // La fila del animal está bloqueada: si otra aprobación ya lo reservó, aquí se ve RESERVADO.
        if (animal.estado() != EstadoAnimal.DISPONIBLE) {
            throw new AnimalNoDisponibleException();
        }

        Solicitud aprobada = cambiarEstado(solicitud, EstadoSolicitud.APROBADA, vendedor.id(), ahora);
        animalRepository.guardar(animal.conEstado(EstadoAnimal.RESERVADO));

        solicitudRepository.listarPendientesPorAnimal(animal.id()).stream()
                .filter(otra -> !otra.id().equals(solicitud.id()))
                .forEach(otra -> cambiarEstado(otra, EstadoSolicitud.RECHAZADA, vendedor.id(), ahora));

        notificarAprobacion(aprobada, animal, vendedor);
        return aprobada;
    }

    private Solicitud cambiarEstado(Solicitud solicitud, EstadoSolicitud nuevo, Long usuarioId, LocalDateTime fecha) {
        Solicitud guardada = solicitudRepository.guardar(solicitud.conDecision(nuevo, fecha));
        historialRepository.registrar(
                HistorialSolicitud.de(solicitud.id(), solicitud.estado(), nuevo, usuarioId, fecha));
        return guardada;
    }

    /** Un fallo del correo no revierte la aprobación: solo se registra en el log. */
    private void notificarAprobacion(Solicitud aprobada, Animal animal, Usuario vendedor) {
        try {
            Usuario adoptante = usuarioRepository.buscarPorId(aprobada.solicitanteId()).orElse(null);
            if (adoptante == null) {
                log.warn("Solicitud {} aprobada pero no se encontró al solicitante; no se enviaron correos",
                        aprobada.id());
                return;
            }
            PlantillasCorreo.Correo paraAdoptante = PlantillasCorreo.solicitudAprobadaParaAdoptante(
                    adoptante.nombre(), animal.nombre(),
                    vendedor.nombre(), vendedor.email(), vendedor.telefono(), vendedor.ciudad());
            emailSender.enviar(adoptante.email(), paraAdoptante.asunto(), paraAdoptante.cuerpo());

            PlantillasCorreo.Correo paraVendedor = PlantillasCorreo.solicitudAprobadaParaVendedor(
                    vendedor.nombre(), animal.nombre(),
                    adoptante.nombre(), adoptante.email(), adoptante.telefono(), adoptante.ciudad());
            emailSender.enviar(vendedor.email(), paraVendedor.asunto(), paraVendedor.cuerpo());
        } catch (RuntimeException e) {
            log.error("No se pudieron enviar los correos de la solicitud {} aprobada ({})",
                    aprobada.id(), e.getClass().getSimpleName());
        }
    }
}
