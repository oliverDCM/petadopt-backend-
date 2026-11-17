package com.petadopt.application.service;

import com.petadopt.application.port.in.CambiarEstadoAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.EmailSenderPort;
import com.petadopt.application.port.out.HistorialSolicitudRepositoryPort;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
import com.petadopt.domain.exception.TransicionEstadoInvalidaException;
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
public class CambiarEstadoAnimalService implements CambiarEstadoAnimalUseCase {

    private static final Logger log = LoggerFactory.getLogger(CambiarEstadoAnimalService.class);
    private static final String SOLO_DUENO = "Solo el vendedor dueño puede cambiar el estado del animal";

    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final SolicitudRepositoryPort solicitudRepository;
    private final HistorialSolicitudRepositoryPort historialRepository;
    private final EmailSenderPort emailSender;

    public CambiarEstadoAnimalService(AnimalRepositoryPort animalRepository,
                                      UsuarioRepositoryPort usuarioRepository,
                                      SolicitudRepositoryPort solicitudRepository,
                                      HistorialSolicitudRepositoryPort historialRepository,
                                      EmailSenderPort emailSender) {
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.historialRepository = historialRepository;
        this.emailSender = emailSender;
    }

    @Override
    @Transactional
    public Animal cambiar(Comando comando) {
        Usuario vendedor = usuarioRepository.buscarPorEmail(comando.vendedorEmail())
                .orElseThrow(() -> new NoEsDuenoDelAnimalException(SOLO_DUENO));

        Animal animal = animalRepository.buscarPorIdParaActualizar(comando.animalId())
                .orElseThrow(() -> new AnimalNoEncontradoException(comando.animalId()));

        if (!animal.vendedorId().equals(vendedor.id())) {
            throw new NoEsDuenoDelAnimalException(SOLO_DUENO);
        }
        if (!animal.estado().puedePasarA(comando.nuevoEstado())) {
            throw new TransicionEstadoInvalidaException(animal.estado(), comando.nuevoEstado());
        }

        Animal guardado = animalRepository.guardar(animal.conEstado(comando.nuevoEstado()));
        if (comando.nuevoEstado() == EstadoAnimal.DISPONIBLE || comando.nuevoEstado() == EstadoAnimal.RETIRADO) {
            cerrarSolicitudesActivas(guardado, vendedor);
        }
        return guardado;
    }

    /**
     * Liberar una reserva o retirar la publicación deja sin efecto las solicitudes que seguían vivas:
     * la aprobada (ya no hay reserva) y las pendientes de un animal retirado. Sin esto quedarían
     * "aprobadas" o "pendientes" para siempre, y podrían coexistir dos aprobadas para el mismo animal.
     * La fila del animal está bloqueada, así que no puede entrar otra solicitud ni otra decisión a la vez.
     */
    private void cerrarSolicitudesActivas(Animal animal, Usuario vendedor) {
        LocalDateTime ahora = LocalDateTime.now();
        for (Solicitud solicitud : solicitudRepository.listarActivasPorAnimal(animal.id())) {
            // Al volver a DISPONIBLE, las pendientes (si las hubiera) siguen su curso; solo cae la reserva.
            if (animal.estado() == EstadoAnimal.DISPONIBLE && solicitud.estado() != EstadoSolicitud.APROBADA) {
                continue;
            }
            solicitudRepository.guardar(solicitud.conDecision(EstadoSolicitud.RECHAZADA, ahora));
            historialRepository.registrar(HistorialSolicitud.de(solicitud.id(), solicitud.estado(),
                    EstadoSolicitud.RECHAZADA, vendedor.id(), ahora));
            avisar(solicitud, animal);
        }
    }

    /** Un fallo del correo no revierte el cambio de estado: solo se registra en el log. */
    private void avisar(Solicitud solicitud, Animal animal) {
        try {
            usuarioRepository.buscarPorId(solicitud.solicitanteId()).ifPresent(adoptante -> {
                PlantillasCorreo.Correo correo = PlantillasCorreo.solicitudCerrada(
                        adoptante.nombre(), animal.nombre(), solicitud.estado() == EstadoSolicitud.APROBADA);
                emailSender.enviar(adoptante.email(), correo.asunto(), correo.cuerpo());
            });
        } catch (RuntimeException e) {
            log.error("No se pudo avisar el cierre de la solicitud {} ({})",
                    solicitud.id(), e.getClass().getSimpleName());
        }
    }
}
