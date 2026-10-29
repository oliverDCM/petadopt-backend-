package com.petadopt.application.service;

import com.petadopt.application.port.in.ConsultarContactoUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.SolicitudRepositoryPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.ContactoNoDisponibleException;
import com.petadopt.domain.exception.SolicitudNoEncontradaException;
import com.petadopt.domain.exception.UsuarioNoEncontradoException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.Contacto;
import com.petadopt.domain.model.EstadoSolicitud;
import com.petadopt.domain.model.Solicitud;
import com.petadopt.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarContactoService implements ConsultarContactoUseCase {

    private final SolicitudRepositoryPort solicitudRepository;
    private final AnimalRepositoryPort animalRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public ConsultarContactoService(SolicitudRepositoryPort solicitudRepository,
                                    AnimalRepositoryPort animalRepository,
                                    UsuarioRepositoryPort usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Contacto consultar(String emailUsuario, Long solicitudId) {
        Usuario usuario = usuarioRepository.buscarPorEmail(emailUsuario)
                .orElseThrow(ContactoNoDisponibleException::new);

        Solicitud solicitud = solicitudRepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new SolicitudNoEncontradaException(solicitudId));

        Animal animal = animalRepository.buscarPorId(solicitud.animalId())
                .orElseThrow(() -> new AnimalNoEncontradoException(solicitud.animalId()));

        boolean esSolicitante = usuario.id().equals(solicitud.solicitanteId());
        boolean esDueno = usuario.id().equals(animal.vendedorId());

        if (solicitud.estado() != EstadoSolicitud.APROBADA || (!esSolicitante && !esDueno)) {
            throw new ContactoNoDisponibleException();
        }

        Long otraParteId = esSolicitante ? animal.vendedorId() : solicitud.solicitanteId();
        Usuario otraParte = usuarioRepository.buscarPorId(otraParteId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(otraParteId));
        return Contacto.de(otraParte);
    }
}
