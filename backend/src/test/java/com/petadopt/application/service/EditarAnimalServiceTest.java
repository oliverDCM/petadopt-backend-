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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditarAnimalServiceTest {

    private static final String FOTO_ACTUAL = "https://petadopt-fotos.s3.us-east-1.amazonaws.com/petadopt/animales/actual.jpg";
    private static final String FOTO_NUEVA = "https://petadopt-fotos.s3.us-east-1.amazonaws.com/petadopt/animales/nueva.png";

    @Mock
    private AnimalRepositoryPort animalRepository;
    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private ImagenStoragePort imagenStorage;

    private EditarAnimalService service;

    private final Usuario vendedor = new Usuario(1L, "Vendedor", "v@test.com", "hash", "300", "Bogotá", Rol.VENDEDOR);
    private final LocalDateTime publicado = LocalDateTime.of(2026, 10, 1, 9, 0);

    @BeforeEach
    void preparar() {
        // Ejecuta el bloque "transaccional" directamente, sin base de datos
        service = new EditarAnimalService(animalRepository, usuarioRepository, imagenStorage,
                TransactionOperations.withoutTransaction());
    }

    private Animal animal(long vendedorId, EstadoAnimal estado) {
        return new Animal(10L, "Firulais", EspecieAnimal.PERRO, "Criollo", 12, SexoAnimal.MACHO,
                "Cariñoso", FOTO_ACTUAL, estado, "Bogotá", vendedorId, publicado,
                false, false, TipoPublicacion.ADOPCION);
    }

    private EditarAnimalUseCase.Comando comando(String fotoUrl, ImagenArchivo foto) {
        return new EditarAnimalUseCase.Comando("v@test.com", 10L, "  Rocky  ", EspecieAnimal.PERRO, "Labrador",
                24, SexoAnimal.MACHO, "Juguetón", fotoUrl, "Fusagasugá", true, true, TipoPublicacion.VENTA, foto);
    }

    private ImagenArchivo fotoPng() {
        return new ImagenArchivo("rocky.png", "image/png", ImagenesDePrueba.pngConMetadatos());
    }

    /** El dueño y su animal DISPONIBLE, tanto en la comprobación previa como al bloquear la fila. */
    private void prepararEdicionValida() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.DISPONIBLE)));
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.DISPONIBLE)));
        when(animalRepository.guardar(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void actualiza_los_datos_y_conserva_id_estado_vendedor_fecha_y_foto() {
        prepararEdicionValida();

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
    void con_foto_nueva_la_sube_sin_metadatos_y_borra_la_anterior() {
        prepararEdicionValida();
        when(imagenStorage.subirImagen(any(ImagenArchivo.class))).thenReturn(FOTO_NUEVA);
        when(imagenStorage.esUrlPropia(FOTO_ACTUAL)).thenReturn(true);

        Animal resultado = service.editar(comando(null, fotoPng()));

        assertThat(resultado.fotoUrl()).isEqualTo(FOTO_NUEVA);
        ArgumentCaptor<ImagenArchivo> subida = ArgumentCaptor.forClass(ImagenArchivo.class);
        verify(imagenStorage).subirImagen(subida.capture());
        assertThat(subida.getValue().getContentType()).isEqualTo("image/png");
        assertThat(subida.getValue().getContenido()).isEqualTo(ImagenesDePrueba.png()); // sin texto, fecha ni EXIF
        verify(imagenStorage).eliminarImagen(FOTO_ACTUAL);
    }

    @Test
    void la_foto_se_sube_antes_de_bloquear_la_fila_del_animal() {
        prepararEdicionValida();
        when(imagenStorage.subirImagen(any(ImagenArchivo.class))).thenReturn(FOTO_NUEVA);

        service.editar(comando(null, fotoPng()));

        InOrder orden = inOrder(imagenStorage, animalRepository);
        orden.verify(imagenStorage).subirImagen(any(ImagenArchivo.class));
        orden.verify(animalRepository).buscarPorIdParaActualizar(10L);
    }

    @Test
    void reenviar_el_enlace_de_la_foto_actual_la_conserva() {
        prepararEdicionValida();

        Animal resultado = service.editar(comando(FOTO_ACTUAL, null));

        assertThat(resultado.fotoUrl()).isEqualTo(FOTO_ACTUAL);
        verifyNoInteractions(imagenStorage);
    }

    @Test
    void rechaza_un_enlace_a_una_foto_de_otro_servidor() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.DISPONIBLE)));
        when(imagenStorage.esUrlPropia("https://ejemplo.com/otra.jpg")).thenReturn(false);

        assertThrows(ImagenInvalidaException.class, () -> service.editar(comando("https://ejemplo.com/otra.jpg", null)));
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_una_imagen_con_formato_no_permitido() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.DISPONIBLE)));
        ImagenArchivo gif = new ImagenArchivo("a.gif", "image/gif", new byte[]{'G', 'I', 'F'});

        assertThrows(ImagenInvalidaException.class, () -> service.editar(comando(null, gif)));
        verifyNoInteractions(imagenStorage);
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_una_imagen_danada_aunque_empiece_como_un_png() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.DISPONIBLE)));
        ImagenArchivo rota = new ImagenArchivo("rota.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0});

        assertThrows(ImagenInvalidaException.class, () -> service.editar(comando(null, rota)));
        verifyNoInteractions(imagenStorage);
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void no_permite_editar_un_animal_que_no_esta_disponible_y_no_llega_a_subir_la_foto() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.RESERVADO)));

        assertThrows(AnimalNoDisponibleException.class, () -> service.editar(comando(null, fotoPng())));
        verifyNoInteractions(imagenStorage);
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_a_quien_no_es_dueno_y_no_llega_a_subir_la_foto() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.of(animal(99L, EstadoAnimal.DISPONIBLE)));

        assertThrows(NoEsDuenoDelAnimalException.class, () -> service.editar(comando(null, fotoPng())));
        verifyNoInteractions(imagenStorage);
        verify(animalRepository, never()).guardar(any());
    }

    @Test
    void rechaza_animal_inexistente() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.empty());

        assertThrows(AnimalNoEncontradoException.class, () -> service.editar(comando(null, null)));
    }

    @Test
    void si_el_animal_deja_de_estar_disponible_mientras_sube_la_foto_no_guarda_y_borra_la_foto_subida() {
        when(usuarioRepository.buscarPorEmail("v@test.com")).thenReturn(Optional.of(vendedor));
        when(animalRepository.buscarPorId(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.DISPONIBLE)));
        // al bloquear la fila, otra petición ya lo reservó
        when(animalRepository.buscarPorIdParaActualizar(10L)).thenReturn(Optional.of(animal(1L, EstadoAnimal.RESERVADO)));
        when(imagenStorage.subirImagen(any(ImagenArchivo.class))).thenReturn(FOTO_NUEVA);
        when(imagenStorage.esUrlPropia(FOTO_NUEVA)).thenReturn(true);

        assertThrows(AnimalNoDisponibleException.class, () -> service.editar(comando(null, fotoPng())));

        verify(animalRepository, never()).guardar(any());
        verify(imagenStorage).eliminarImagen(FOTO_NUEVA);
        verify(imagenStorage, never()).eliminarImagen(FOTO_ACTUAL);
    }
}
