package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.dtos.LoginRequest;
import com.eventnode.eventnodeapi.dtos.LoginResponse;
import com.eventnode.eventnodeapi.models.Rol;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.AlumnoRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import com.eventnode.eventnodeapi.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        Rol rol = new Rol();
        rol.setNombre("ALUMNO");

        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCorreo("test@test.com");
        usuario.setPassword("encodedPassword");
        usuario.setEstado("ACTIVO");
        usuario.setRol(rol);
        usuario.setNombre("Test");
        usuario.setApellidoPaterno("User");

        loginRequest = new LoginRequest();
        loginRequest.setCorreo("test@test.com");
        loginRequest.setPassword("rawPassword");
    }

    @Test
    void login_Success() {
        // Arrange
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(UsernamePasswordAuthenticationToken.class))).thenReturn("fake-jwt-token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("ALUMNO", response.getRol());
        verify(passwordEncoder).matches(loginRequest.getPassword(), usuario.getPassword());
        verify(jwtTokenProvider).generateToken(any());
        verify(usuarioRepository).save(usuario); // Check attempts reset
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(usuarioRepository).save(usuario); // Check attempts incremented
    }

    @Test
    void login_Success_AsAlumno() {
        // Arrange
        usuario.getRol().setNombre("ALUMNO");
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any())).thenReturn("fake-jwt-token");
        when(alumnoRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("ALUMNO", response.getRol());
        verify(alumnoRepository).findById(1);
    }
}
