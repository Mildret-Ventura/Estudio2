package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.dtos.PerfilResponse;
import com.eventnode.eventnodeapi.models.Alumno;
import com.eventnode.eventnodeapi.models.Rol;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.AdministradorRepository;
import com.eventnode.eventnodeapi.repositories.AlumnoRepository;
import com.eventnode.eventnodeapi.repositories.RolRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class UsuarioServiceTest {

    private static UsuarioService instancia;

    private UsuarioRepository usuarioRepository;
    private AlumnoRepository alumnoRepository;
    private AdministradorRepository administradorRepository;
    private RolRepository rolRepository;
    private PasswordEncoder passwordEncoder;

    private Integer idUsuario;
    private Usuario usuarioEntrada;
    private Alumno alumnoEntrada;
    private PerfilResponse perfilResultado;

    @BeforeAll
    public static void iniciarInstancia() {
        instancia = null;
    }

    @BeforeEach
    void resetearVariables() {
        idUsuario = 1;
        perfilResultado = null;

        usuarioRepository = mock(UsuarioRepository.class);
        alumnoRepository = mock(AlumnoRepository.class);
        administradorRepository = mock(AdministradorRepository.class);
        rolRepository = mock(RolRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        instancia = new UsuarioService(
                usuarioRepository,
                alumnoRepository,
                administradorRepository,
                rolRepository,
                passwordEncoder
        );

        usuarioEntrada = new Usuario();
        usuarioEntrada.setIdUsuario(idUsuario);
        usuarioEntrada.setNombre("Juan");
        usuarioEntrada.setApellidoPaterno("Pérez");
        usuarioEntrada.setApellidoMaterno("López");
        usuarioEntrada.setCorreo("juan.perez@test.com");
        usuarioEntrada.setEstado("ACTIVO");
        Rol rol = new Rol();
        rol.setNombre("ALUMNO");
        usuarioEntrada.setRol(rol);

        alumnoEntrada = new Alumno();
        alumnoEntrada.setIdUsuario(idUsuario);
        alumnoEntrada.setMatricula("A01234567");
        alumnoEntrada.setFechaNac(LocalDate.now().minusYears(20));
        alumnoEntrada.setEdad(20);
        alumnoEntrada.setSexo("M");
        alumnoEntrada.setCuatrimestre(4);
    }

    @Test
    void visualizacionCorrectaDatosPerfilTest() {
        // Preparar datos
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuarioEntrada));
        when(alumnoRepository.findById(idUsuario)).thenReturn(Optional.of(alumnoEntrada));

        // Invocar método
        perfilResultado = instancia.obtenerPerfil(idUsuario);

        // Validar resultado
        assertNotNull(perfilResultado);
        assertEquals(idUsuario, perfilResultado.getIdUsuario());
        assertEquals("Juan", perfilResultado.getNombre());
        assertEquals("juan.perez@test.com", perfilResultado.getCorreo());
        assertEquals("ALUMNO", perfilResultado.getRol());
        assertEquals("A01234567", perfilResultado.getMatricula());
    }

    @Test
    void listarTodosUsuariosVacioTest() {
        // Preparar datos
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        // Invocar método
        var lista = instancia.listarTodos();

        // Validar resultado
        assertTrue(lista.isEmpty());
    }

    @Test
    void cambiarEstadoUsuarioActivoAInactivoTest() {
        // Preparar datos
        usuarioEntrada.setEstado("ACTIVO");
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuarioEntrada));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioEntrada);

        // Invocar método
        instancia.cambiarEstado(idUsuario);

        // Validar resultado
        assertEquals("INACTIVO", usuarioEntrada.getEstado());
    }
}
