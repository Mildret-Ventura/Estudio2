package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class PasswordRecoveryServiceTest {

    private static PasswordRecoveryService instancia;

    private UsuarioRepository usuarioRepository;
    private JavaMailSender mailSender;
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;
    private String correo;

    @BeforeAll
    public static void iniciarInstancia() {
        instancia = null;
    }

    @BeforeEach
    void resetearVariables() {
        correo = "user@test.com";
        usuarioRepository = mock(UsuarioRepository.class);
        mailSender = mock(JavaMailSender.class);
        passwordEncoder = mock(PasswordEncoder.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        instancia = new PasswordRecoveryService(usuarioRepository, mailSender, passwordEncoder);

        usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setEstado("ACTIVO");
        usuario.setNombre("Test");
    }

    @Test
    void enviarCodigoExitosoTest() {
        // Preparar datos
        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Invocar método
        assertDoesNotThrow(() -> instancia.enviarCodigo(correo));

        // Validar resultado
        assertNotNull(usuario.getRecoverPassword());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void enviarCodigoCorreoNoRegistradoTest() {
        // Preparar datos
        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        // Invocar método
        assertThrows(IllegalArgumentException.class, () -> instancia.enviarCodigo(correo));
    }

    @Test
    void verificarCodigoCorrectoTest() {
        // Preparar datos
        usuario.setRecoverPassword("123456");
        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));

        // Invocar método
        assertDoesNotThrow(() -> instancia.verificarCodigo(correo, "123456"));
    }

    @Test
    void verificarCodigoIncorrectoTest() {
        // Preparar datos
        usuario.setRecoverPassword("111111");
        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));

        // Invocar método
        assertThrows(IllegalArgumentException.class, () -> instancia.verificarCodigo(correo, "999999"));
    }

    @Test
    void restablecerPasswordValidaTest() {
        // Preparar datos
        usuario.setRecoverPassword("123456");
        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Invocar método
        assertDoesNotThrow(() -> instancia.restablecerPassword(correo, "123456", "NuevaPass1!"));

        // Validar resultado
        assertNull(usuario.getRecoverPassword());
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void restablecerPasswordMuyCortaTest() {
        // Preparar datos
        usuario.setRecoverPassword("123456");
        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));

        // Invocar método
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> instancia.restablecerPassword(correo, "123456", "Corta1!"));

        // Validar resultado
        assertTrue(ex.getMessage().contains("8 caracteres"));
    }
}
