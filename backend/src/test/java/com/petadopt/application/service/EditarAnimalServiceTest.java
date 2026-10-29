package com.petadopt.application.service;

import com.petadopt.application.port.in.EditarAnimalUseCase;
import com.petadopt.application.port.out.AnimalRepositoryPort;
import com.petadopt.application.port.out.ImagenStoragePort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.AnimalNoDisponibleException;
import com.petadopt.domain.exception.AnimalNoEncontradoException;
import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.exception.NoEsDuenoDelAnimalException;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditarAnimalServiceTest {

    private static final String FOTO_ACTUAL = "https://res.cloudinary.com/demo/actual.jpg";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};

    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private ImagenStoragePort imagenStorage;
    @InjectMocks
    private EditarAnimalService service;

    private final Usuario vendedor = new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);
    private final LocalDateTime publicado = LocalDateTime.of(2026, 10, 1, 9, 0);

    private Animal animal(long vendedorId, EstadoAnimal estado) {
        return new Animal(10L, "Firulais", EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.MACHO,
                "Cariñoso", FOTO_ACTUAL, estado, "Bogotá", vendedorId, publicado,
                false, false, TipoPublicacion.ADOPCION);
    }

    private EditarAnimalUseCase.Comando comando(String fotoUrl, ImagenArchivo foto) {
        return new EditarAnimalUseCase.Comando("v@test.com", 10L, "  Rocky  ", EspecieAnimal.PERRO, "Labrador",
                24, SexoAnimal.MACHO, "Juguetón", fotoUrl, "Fusagasugá", true, true, TipoPublicacion.VENTA, foto);
    }

    private void prepararDueno(EstadoAnimal estado) {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, estado)));
    }

    @Test
    void actualiza_los_datos_y_conserva_id_estado_vendedor_fecha_y_foto() {
        prepararDueno(EstadoAnimal.DISPONIBLE);
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal resultado = service.editar(comando(null, null));

        assertThat(resultado.nombre()).isEqualTo("Rocky");
        assertThat(resultado.raza()).isEqualTo("Labrador");
        assertThat(resultado.edadMeses()).isEqualTo(24);
        assertThat(resultado.ciudad()).isEqualTo("Fusagasugá");
        assertThat(resultado.vacunado()).isTrue();
        assertThat(resultado.tipo()).isEqualTo(TipoPublicacion.VENTA);
        assertThat(resultado.id()).isEqualTo(10L);
        assertThat(resultado.estado()).isEqualTo(EstadoAnimal.DISPONIBLE);
        assertThat(resultado.vendedorId()).isEqualTo(1L);
        assertThat(resultado.fechaPublicacion()).isEqualTo(publicado);
        assertThat(resultado.fotoUrl()).isEqualTo(FOTO_ACTUAL);
        verifyNoInteractions(imagenStorage);
    }

    @Test
    void con_foto_nueva_la_sube_y_reemplaza_la_anterior() {
        prepararDueno(EstadoAnimal.DISPONIBLE);
        ImagenArchivo foto = new ImagenArchivo("rocky.png", "image/png", PNG);
        when(imagenStorage.subirImagen(foto)).thenReturn("https://res.cloudinary.com/demo/nueva.png");
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal resultado = service.editar(comando(null, foto));

        assertThat(resultado.fotoUrl()).isEqualTo("https://res.cloudinary.com/demo/nueva.png");
    }

    @Test
    void con_url_nueva_y_sin_archivo_usa_la_url() {
        prepararDueno(EstadoAnimal.DISPONIBLE);
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal resultado = service.editar(comando("https://ejemplo.com/otra.jpg", null));

        assertThat(resultado.fotoUrl()).isEqualTo("https://ejemplo.com/otra.jpg");
    }

    @Test
    void rechaza_una_imagen_con_formato_no_permitido() {
        prepararDueno(EstadoAnimal.DISPONIBLE);
        ImagenArchivo gif = new ImagenArchivo("a.gif", "image/gif", new byte[]{'G', 'I', 'F'});

        assertThrows(ImagenInvalidaException.class, () -> service.editar(comando(null, gif)));
        verifyNoInteractions(imagenStorage);
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void no_permite_editar_un_animal_que_no_esta_disponible() {
        prepararDueno(EstadoAnimal.RESERVADO);

        assertThrows(AnimalNoDisponibleException.class, () -> service.editar(comando(null, null)));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_a_quien_no_es_dueno() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(99L, EstadoAnimal.DISPONIBLE)));

        assertThrows(NoEsDuenoDelAnimalException.class, () -> service.editar(comando(null, null)));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_animal_inexistente() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.empty());

        assertThrows(AnimalNoEncontradoException.class, () -> service.editar(comando(null, null)));
    }
}
