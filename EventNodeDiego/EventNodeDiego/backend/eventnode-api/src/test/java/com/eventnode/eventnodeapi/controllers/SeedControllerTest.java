package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.models.Rol;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.AdministradorRepository;
import com.eventnode.eventnodeapi.repositories.RolRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RolRepository rolRepository;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private AdministradorRepository administradorRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private Usuario usuarioAdminExistente;
    private MvcResult resultadoMvc;
    private String mensajeRespuesta;

    @BeforeAll
    static void inicializarClase() {
        // Constante de correo fijo del seed
    }

    @BeforeEach
    void reiniciarVariables() {
        Mockito.reset(rolRepository, usuarioRepository, administradorRepository, passwordEncoder);

        usuarioAdminExistente = new Usuario();
        usuarioAdminExistente.setCorreo("admin@eventnode.com");

        resultadoMvc = null;
        mensajeRespuesta = null;
    }

    @Test
    void seedInitAdminYaExisteActualizaPasswordTest() throws Exception {
        when(usuarioRepository.findByCorreo("admin@eventnode.com")).thenReturn(Optional.of(usuarioAdminExistente));
        when(passwordEncoder.encode(any())).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioAdminExistente);
        when(rolRepository.findByNombre(any())).thenAnswer(inv -> {
            Rol r = new Rol();
            r.setNombre(inv.getArgument(0));
            return Optional.of(r);
        });

        resultadoMvc = mockMvc.perform(post("/api/seed/init")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        mensajeRespuesta = JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje");
        assertTrue(mensajeRespuesta.contains("actualizado"));
        assertFalse(mensajeRespuesta.isEmpty());
    }

    @Test
    void seedInitAdminNoExisteCreaDatosTest() throws Exception {
        when(usuarioRepository.findByCorreo("admin@eventnode.com")).thenReturn(Optional.empty());
        when(rolRepository.findByNombre("ALUMNO")).thenReturn(Optional.empty());
        when(rolRepository.findByNombre("ADMINISTRADOR")).thenReturn(Optional.empty());
        Rol rolSuper = new Rol();
        rolSuper.setIdRol(3);
        rolSuper.setNombre("SUPERADMIN");
        when(rolRepository.findByNombre("SUPERADMIN"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(rolSuper));
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        Usuario guardado = new Usuario();
        guardado.setIdUsuario(1);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);
        when(administradorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        resultadoMvc = mockMvc.perform(post("/api/seed/init")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        mensajeRespuesta = JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje");
        assertTrue(mensajeRespuesta.contains("creados"));
    }
}
