package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.dtos.AdminRegistroRequest;
import com.eventnode.eventnodeapi.dtos.PerfilResponse;
import com.eventnode.eventnodeapi.services.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    private PerfilResponse perfilEjemplo;
    private AdminRegistroRequest solicitudRegistrarAdmin;
    private String jsonSolicitud;
    private MvcResult resultadoMvc;

    @BeforeAll
    static void inicializarClase() {
        // Sin estado global
    }

    @BeforeEach
    void reiniciarVariables() {
        Mockito.reset(usuarioService);

        perfilEjemplo = new PerfilResponse();
        perfilEjemplo.setIdUsuario(1);
        perfilEjemplo.setNombre("Ana");
        perfilEjemplo.setCorreo("ana@test.com");
        perfilEjemplo.setRol("ALUMNO");

        solicitudRegistrarAdmin = new AdminRegistroRequest();
        solicitudRegistrarAdmin.setNombre("Admin");
        solicitudRegistrarAdmin.setApellidoPaterno("Sistema");
        solicitudRegistrarAdmin.setCorreo("admin2@test.com");
        solicitudRegistrarAdmin.setPassword("Password1");
        solicitudRegistrarAdmin.setIdSolicitante(1);

        jsonSolicitud = null;
        resultadoMvc = null;
    }

    @Test
    void listarUsuariosRetornaListaTest() throws Exception {
        when(usuarioService.listarTodos()).thenReturn(List.of(perfilEjemplo));

        resultadoMvc = mockMvc.perform(get("/api/usuarios")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("Ana", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$[0].nombre"));
    }

    @Test
    void listarUsuariosListaVaciaRetornaOkTest() throws Exception {
        when(usuarioService.listarTodos()).thenReturn(Collections.emptyList());

        resultadoMvc = mockMvc.perform(get("/api/usuarios")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("[]", resultadoMvc.getResponse().getContentAsString().trim());
    }

    @Test
    void obtenerPerfilExistenteRetornaOkTest() throws Exception {
        when(usuarioService.obtenerPerfil(1)).thenReturn(perfilEjemplo);

        resultadoMvc = mockMvc.perform(get("/api/usuarios/1/perfil")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("ana@test.com", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.correo"));
    }

    @Test
    void obtenerPerfilNoExisteRetornaBadRequestTest() throws Exception {
        when(usuarioService.obtenerPerfil(99)).thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        resultadoMvc = mockMvc.perform(get("/api/usuarios/99/perfil")).andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertEquals("Usuario no encontrado",
                JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void registrarAdminValidoRetornaOkTest() throws Exception {
        when(usuarioService.registrarAdmin(any(AdminRegistroRequest.class))).thenReturn(perfilEjemplo);
        jsonSolicitud = objectMapper.writeValueAsString(solicitudRegistrarAdmin);

        resultadoMvc = mockMvc.perform(post("/api/usuarios/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void registrarAdminPasswordCortaRetornaValidacionTest() throws Exception {
        solicitudRegistrarAdmin.setPassword("corta");
        jsonSolicitud = objectMapper.writeValueAsString(solicitudRegistrarAdmin);

        resultadoMvc = mockMvc.perform(post("/api/usuarios/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertFalse(resultadoMvc.getResponse().getContentAsString().isEmpty());
    }

    @Test
    void registrarAdminProhibidoRetorna403Test() throws Exception {
        when(usuarioService.registrarAdmin(any(AdminRegistroRequest.class)))
                .thenThrow(new SecurityException("No autorizado"));
        jsonSolicitud = objectMapper.writeValueAsString(solicitudRegistrarAdmin);

        resultadoMvc = mockMvc.perform(post("/api/usuarios/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(403, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void registrarAdminCorreoDuplicadoRetornaBadRequestTest() throws Exception {
        when(usuarioService.registrarAdmin(any(AdminRegistroRequest.class)))
                .thenThrow(new IllegalArgumentException("Correo en uso"));
        jsonSolicitud = objectMapper.writeValueAsString(solicitudRegistrarAdmin);

        resultadoMvc = mockMvc.perform(post("/api/usuarios/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void cambiarEstadoUsuarioExistenteRetornaOkTest() throws Exception {
        doNothing().when(usuarioService).cambiarEstado(1);

        resultadoMvc = mockMvc.perform(patch("/api/usuarios/1/estado")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertTrue(JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje")
                .toString().contains("Estado"));
    }

    @Test
    void cambiarEstadoUsuarioInexistenteRetornaBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("No existe")).when(usuarioService).cambiarEstado(2);

        resultadoMvc = mockMvc.perform(patch("/api/usuarios/2/estado")).andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }
}
