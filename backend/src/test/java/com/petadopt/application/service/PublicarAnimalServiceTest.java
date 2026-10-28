package com.petadopt.application.service;

import com.petadopt.application.port.in.PublicarAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.ImagenStoragePort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AlmacenamientoImagenException;
import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.exception.VendedorNoAutorizadoException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.ImagenArchivo;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.TipoPublicacion;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicarAnimalServiceTest {

    private static final String URL_FOTO = "https://res.cloudinary.com/demo/image/upload/petadopt/firulais.jpg";

    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private ImagenStoragePort imagenStorage;
    @InjectMocks
    private PublicarAnimalService service;

    private Usuario vendedor() {
        return new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);
    }

    private PublicarAnimalUseCase.Comando comando(String email, ImagenArchivo foto) {
        return new PublicarAnimalUseCase.Comando(email, "  Firulais  ", EspecieAnimal.PERRO,
                "Criollo", 12, SexoAnimal.MACHO, "Muy cariñoso", null, "Fusagasugá",
                true, false, TipoPublicacion.ADOPCION, foto);
    }

    private PublicarAnimalUseCase.Comando comando(String email) {
        return comando(email, null);
    }

    @Test
    void publica_animal_disponible_asociado_al_vendedor() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor()));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal resultado = service.publicar(comando("v@test.com"));

        assertThat(resultado.nombre()).isEqualTo("Firulais");
        assertThat(resultado.estado()).isEqualTo(EstadoAnimal.DISPONIBLE);
        assertThat(resultado.vendedorId()).isEqualTo(1L);
        assertThat(resultado.vacunado()).isTrue();
        assertThat(resultado.esterilizado()).isFalse();
        assertThat(resultado.tipo()).isEqualTo(TipoPublicacion.ADOPCION);
        assertThat(resultado.fotoUrl()).isNull();
        verifyNoInteractions(imagenStorage);
    }

    @Test
    void rechaza_usuario_que_no_es_vendedor() {
        Usuario adoptante = new Usuario(2L, "Adoptante", "a@test.com", "hash", "300", "Bogotá", Rol.ADOPTANTE);
        when(usuarioRepository.buscarPorEmail("a@test.com")).thenReturn(Optional.of(adoptante));

        assertThrows(VendedorNoAutorizadoException.class, () -> service.publicar(comando("a@test.com")));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_usuario_inexistente() {
        when(usuarioRepository.buscarPorEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThrows(VendedorNoAutorizadoException.class, () -> service.publicar(comando("nadie@test.com")));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void sube_la_imagen_y_guarda_la_url_devuelta() {
        ImagenArchivo foto = new ImagenArchivo("firulais.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor()));
        when(imagenStorage.subirImagen(foto)).thenReturn(URL_FOTO);
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal resultado = service.publicar(comando("v@test.com", foto));

        assertThat(resultado.fotoUrl()).isEqualTo(URL_FOTO);
    }

    @Test
    void rechaza_un_formato_de_imagen_no_permitido() {
        ImagenArchivo foto = new ImagenArchivo("animado.gif", "image/gif", new byte[]{1, 2, 3});
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor()));

        assertThrows(ImagenInvalidaException.class, () -> service.publicar(comando("v@test.com", foto)));
        verifyNoInteractions(imagenStorage);
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_una_imagen_que_supera_el_tamano_maximo() {
        ImagenArchivo foto = new ImagenArchivo("grande.jpg", "image/jpeg", new byte[5 * 1024 * 1024 + 1]);
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor()));

        assertThrows(ImagenInvalidaException.class, () -> service.publicar(comando("v@test.com", foto)));
        verifyNoInteractions(imagenStorage);
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void no_publica_el_animal_si_el_servicio_externo_falla() {
        ImagenArchivo foto = new ImagenArchivo("firulais.png", "image/png", new byte[]{1, 2, 3});
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor()));
        when(imagenStorage.subirImagen(foto)).thenThrow(new AlmacenamientoImagenException(new RuntimeException("sin red")));

        assertThrows(AlmacenamientoImagenException.class, () -> service.publicar(comando("v@test.com", foto)));
        verify(animalRepository, never()).guardar(any());
    }
}
