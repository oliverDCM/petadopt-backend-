package com.petadopt.infrastructure.adapter.in.web;

import com.petadopt.application.port.in.CambiarEstadoAnimalUseCase;
import com.petadopt.application.port.in.ConsultarAnimalUseCase;
import com.petadopt.application.port.in.EditarAnimalUseCase;
import com.petadopt.application.port.in.ListarAnimalesUseCase;
import com.petadopt.application.port.in.ListarMisAnimalesUseCase;
import com.petadopt.application.port.in.PublicarAnimalUseCase;
import com.petadopt.domain.exception.ImagenInvalidaException;
import com.petadopt.domain.model.Animal;
import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.SexoAnimal;
import com.petadopt.domain.model.TipoPublicacion;
import com.petadopt.infrastructure.adapter.in.web.dto.AnimalDetalleResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.AnimalRequest;
import com.petadopt.infrastructure.adapter.in.web.dto.AnimalResponse;
import com.petadopt.infrastructure.adapter.in.web.dto.EstadoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnimalControllerTest {

    private final PublicarAnimalUseCase publicar = mock(PublicarAnimalUseCase.class);
    private final ListarAnimalesUseCase listar = mock(ListarAnimalesUseCase.class);
    private final ListarMisAnimalesUseCase listarMios = mock(ListarMisAnimalesUseCase.class);
    private final EditarAnimalUseCase editar = mock(EditarAnimalUseCase.class);
    private final CambiarEstadoAnimalUseCase cambiarEstado = mock(CambiarEstadoAnimalUseCase.class);
    private final ConsultarAnimalUseCase consultar = mock(ConsultarAnimalUseCase.class);

    private AnimalController controller;
    private final Authentication vendedor = new UsernamePasswordAuthenticationToken("v@test.com", null);

    private final Animal animal = new Animal(10L, "Lupe", EspecieAnimal.PERRO, "Criolla", 14, SexoAnimal.HEMBRA,
            "Juguetona", null, EstadoAnimal.DISPONIBLE, "Fusagasugá", 1L, LocalDateTime.of(2026, 10, 1, 10, 0),
            true, false, TipoPublicacion.ADOPCION);

    private final AnimalRequest request = new AnimalRequest("Lupe", EspecieAnimal.PERRO, "Criolla", 14,
            SexoAnimal.HEMBRA, "Juguetona", null, "Fusagasugá", true, false, TipoPublicacion.ADOPCION);

    @BeforeEach
    void setUp() {
        controller = new AnimalController(publicar, listar, listarMios, editar, cambiarEstado, consultar);
    }

    @Test
    void publicar_sin_foto_responde_201_con_el_animal() {
        when(publicar.publicar(any())).thenReturn(animal);

        ResponseEntity<AnimalResponse> r = controller.publicar(request, vendedor);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(r.getBody().nombre()).isEqualTo("Lupe");
        ArgumentCaptor<PublicarAnimalUseCase.Comando> c = ArgumentCaptor.forClass(PublicarAnimalUseCase.Comando.class);
        verify(publicar).publicar(c.capture());
        assertThat(c.getValue().vendedorEmail()).isEqualTo("v@test.com");
        assertThat(c.getValue().foto()).isNull();
    }

    @Test
    void publicar_con_foto_pasa_la_imagen_al_caso_de_uso() {
        when(publicar.publicar(any())).thenReturn(animal);
        MockMultipartFile foto = new MockMultipartFile("foto", "lupe.png", "image/png", new byte[]{1, 2, 3});

        controller.publicarConFoto(request, foto, vendedor);

        ArgumentCaptor<PublicarAnimalUseCase.Comando> c = ArgumentCaptor.forClass(PublicarAnimalUseCase.Comando.class);
        verify(publicar).publicar(c.capture());
        assertThat(c.getValue().foto().getContentType()).isEqualTo("image/png");
        assertThat(c.getValue().foto().tamanoBytes()).isEqualTo(3);
    }

    @Test
    void una_foto_vacia_se_ignora() {
        when(publicar.publicar(any())).thenReturn(animal);
        MockMultipartFile vacia = new MockMultipartFile("foto", "x.png", "image/png", new byte[0]);

        controller.publicarConFoto(request, vacia, vendedor);

        ArgumentCaptor<PublicarAnimalUseCase.Comando> c = ArgumentCaptor.forClass(PublicarAnimalUseCase.Comando.class);
        verify(publicar).publicar(c.capture());
        assertThat(c.getValue().foto()).isNull();
    }

    @Test
    void si_no_se_puede_leer_la_foto_responde_imagen_invalida() throws IOException {
        MultipartFile rota = mock(MultipartFile.class);
        when(rota.isEmpty()).thenReturn(false);
        when(rota.getBytes()).thenThrow(new IOException("disco"));

        assertThrows(ImagenInvalidaException.class, () -> controller.publicarConFoto(request, rota, vendedor));
    }

    @Test
    void listar_pasa_los_filtros_al_caso_de_uso() {
        when(listar.listarDisponibles(any(ListarAnimalesUseCase.Filtro.class))).thenReturn(List.of(animal));

        List<AnimalResponse> r = controller.listar(EspecieAnimal.PERRO, TipoPublicacion.ADOPCION, "fusa");

        assertThat(r).extracting(AnimalResponse::id).containsExactly(10L);
        verify(listar).listarDisponibles(new ListarAnimalesUseCase.Filtro(EspecieAnimal.PERRO, TipoPublicacion.ADOPCION, "fusa"));
    }

    @Test
    void detalle_incluye_los_datos_publicos_del_vendedor() {
        when(consultar.consultar(10L)).thenReturn(new ConsultarAnimalUseCase.Detalle(animal, "Carlos", "Fusagasugá"));

        AnimalDetalleResponse r = controller.detalle(10L);

        assertThat(r.nombre()).isEqualTo("Lupe");
        assertThat(r.vendedorNombre()).isEqualTo("Carlos");
        assertThat(r.vendedorCiudad()).isEqualTo("Fusagasugá");
    }

    @Test
    void mis_publicaciones_usa_el_email_del_token() {
        when(listarMios.listar("v@test.com")).thenReturn(List.of(animal));
        assertThat(controller.misPublicaciones(vendedor)).hasSize(1);
    }

    @Test
    void editar_sin_y_con_foto() {
        when(editar.editar(any())).thenReturn(animal);

        assertThat(controller.editar(10L, request, vendedor).id()).isEqualTo(10L);
        controller.editarConFoto(10L, request, new MockMultipartFile("foto", "a.jpg", "image/jpeg", new byte[]{9}), vendedor);

        ArgumentCaptor<EditarAnimalUseCase.Comando> c = ArgumentCaptor.forClass(EditarAnimalUseCase.Comando.class);
        verify(editar, org.mockito.Mockito.times(2)).editar(c.capture());
        assertThat(c.getAllValues().get(0).foto()).isNull();
        assertThat(c.getAllValues().get(1).foto()).isNotNull();
        assertThat(c.getAllValues().get(1).animalId()).isEqualTo(10L);
    }

    @Test
    void cambiar_estado_envia_el_estado_pedido() {
        when(cambiarEstado.cambiar(any())).thenReturn(animal.conEstado(EstadoAnimal.RETIRADO));

        AnimalResponse r = controller.cambiarEstado(10L, new EstadoRequest(EstadoAnimal.RETIRADO), vendedor);

        assertThat(r.estado()).isEqualTo(EstadoAnimal.RETIRADO);
        verify(cambiarEstado).cambiar(new CambiarEstadoAnimalUseCase.Comando("v@test.com", 10L, EstadoAnimal.RETIRADO));
    }
}
