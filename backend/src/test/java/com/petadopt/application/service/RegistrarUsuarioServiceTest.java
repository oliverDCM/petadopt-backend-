package com.petadopt.application.service;

import com.petadopt.application.port.in.RegistrarUsuarioUseCase;
import com.petadopt.application.port.out.PasswordHasherPort;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.exception.EmailYaRegistradoException;
import com.petadopt.domain.model.Rol;
import com.petadopt.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @InjectMocks
    private RegistrarUsuarioService service;

    private RegistrarUsuarioUseCase.Comando comando() {
        return new RegistrarUsuarioUseCase.Comando("  Ana Pérez  ", "  ANA@Test.com ",
                "12345678", "3001234567", "Bogotá", Rol.ADOPTANTE);
    }

    @Test
    void registra_usuario_normalizando_email_y_cifrando_la_contrasena() {
        when(usuarioRepository.existePorEmail("ana@test.com")).thenReturn(false);
        when(passwordHasher.cifrar("12345678")).thenReturn("hash-bcrypt");
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = service.registrar(comando());

        assertThat(resultado.nombre()).isEqualTo("Ana Pérez");
        assertThat(resultado.email()).isEqualTo("ana@test.com");
        assertThat(resultado.password()).isEqualTo("hash-bcrypt");
        assertThat(resultado.rol()).isEqualTo(Rol.ADOPTANTE);
    }

    @Test
    void rechaza_email_ya_registrado() {
        when(usuarioRepository.existePorEmail("ana@test.com")).thenReturn(true);

        assertThrows(EmailYaRegistradoException.class, () -> service.registrar(comando()));
        verify(usuarioRepository, never()).guardar(any());
        verifyNoInteractions(passwordHasher);
    }
}