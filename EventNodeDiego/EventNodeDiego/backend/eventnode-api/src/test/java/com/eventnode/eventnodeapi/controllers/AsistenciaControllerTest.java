package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.services.AsistenciaService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AsistenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AsistenciaService asistenciaService;

    private Map<String, Object> cuerpoRegistrar;
    private Map<String, Object> cuerpoManual;
    private String jsonSolicitud;
    private MvcResult resultadoMvc;
    private int codigoHttpObtenido;

    @BeforeAll
    static void inicializarClase() {
        // Estructura fija de rutas cubierta por constantes implícitas en cada prueba
    }

    @BeforeEach
    void reiniciarVariables() {
        Mockito.reset(asistenciaService);

        cuerpoRegistrar = new HashMap<>();
        cuerpoRegistrar.put("idUsuario", 1);
        cuerpoRegistrar.put("idEvento", 2);
        cuerpoRegistrar.put("metodo", "QR");

        cuerpoManual = new HashMap<>();
        cuerpoManual.put("matricula", "M123");
        cuerpoManual.put("idEvento", 2);

        jsonSolicitud = null;
        resultadoMvc = null;
        codigoHttpObtenido = 0;
    }

    @Test
    void registrarAsistenciaCamposCompletosRetornaCreadoTest() throws Exception {
        doNothing().when(asistenciaService).registrarAsistencia(anyInt(), anyInt(), anyString());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoRegistrar);

        resultadoMvc = mockMvc.perform(post("/api/asistencias/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        codigoHttpObtenido = resultadoMvc.getResponse().getStatus();
        assertEquals(201, codigoHttpObtenido);
        assertEquals("Asistencia registrada exitosamente",
                JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void registrarAsistenciaCuerpoVacioRetornaBadRequestTest() throws Exception {
        resultadoMvc = mockMvc.perform(post("/api/asistencias/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(resultadoMvc.getResponse().getContentAsString().contains("requeridos"));
    }

    @Test
    void registrarAsistenciaMetodoNuloRetornaBadRequestTest() throws Exception {
        cuerpoRegistrar.remove("metodo");
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoRegistrar);

        resultadoMvc = mockMvc.perform(post("/api/asistencias/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertFalse(JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje").toString().isEmpty());
    }

    @Test
    void registrarAsistenciaArgumentoInvalidoRetornaBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("Evento cerrado"))
                .when(asistenciaService).registrarAsistencia(anyInt(), anyInt(), anyString());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoRegistrar);

        resultadoMvc = mockMvc.perform(post("/api/asistencias/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertEquals("Evento cerrado", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void registrarAsistenciaEstadoInvalidoRetornaConflictoTest() throws Exception {
        doThrow(new IllegalStateException("Ya registrada"))
                .when(asistenciaService).registrarAsistencia(anyInt(), anyInt(), anyString());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoRegistrar);

        resultadoMvc = mockMvc.perform(post("/api/asistencias/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(409, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void registrarAsistenciaManualExitosoRetornaCreadoTest() throws Exception {
        doNothing().when(asistenciaService).registrarAsistenciaManual(anyString(), anyInt());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoManual);

        resultadoMvc = mockMvc.perform(post("/api/asistencias/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(201, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void registrarAsistenciaManualSinMatriculaRetornaBadRequestTest() throws Exception {
        cuerpoManual.remove("matricula");
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoManual);

        resultadoMvc = mockMvc.perform(post("/api/asistencias/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(resultadoMvc.getResponse().getContentAsString().contains("requeridos"));
    }

    @Test
    void listarAsistenciasPorEventoRetornaOkTest() throws Exception {
        when(asistenciaService.listarAsistencias(3)).thenReturn(List.of(Map.of("idAsistencia", 1)));

        resultadoMvc = mockMvc.perform(get("/api/asistencias/evento/3")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertTrue(resultadoMvc.getResponse().getContentAsString().contains("idAsistencia"));
    }

    @Test
    void listarAsistenciasErrorInternoRetorna500Test() throws Exception {
        when(asistenciaService.listarAsistencias(9)).thenThrow(new RuntimeException("db"));

        resultadoMvc = mockMvc.perform(get("/api/asistencias/evento/9")).andReturn();

        assertEquals(500, resultadoMvc.getResponse().getStatus());
        assertEquals("Error interno", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void actualizarEstadoConEstadoRetornaOkTest() throws Exception {
        doNothing().when(asistenciaService).actualizarEstado(eq(5), eq("CONFIRMADO"));

        resultadoMvc = mockMvc.perform(patch("/api/asistencias/5/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CONFIRMADO\"}"))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void actualizarEstadoSinEstadoRetornaBadRequestTest() throws Exception {
        resultadoMvc = mockMvc.perform(patch("/api/asistencias/5/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(resultadoMvc.getResponse().getContentAsString().contains("estado"));
    }

    @Test
    void actualizarEstadoArgumentoInvalidoRetornaBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("No existe"))
                .when(asistenciaService).actualizarEstado(eq(5), anyString());

        resultadoMvc = mockMvc.perform(patch("/api/asistencias/5/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"X\"}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void contarAsistenciasPorEventoRetornaTotalTest() throws Exception {
        when(asistenciaService.contarAsistencias(7)).thenReturn(11L);

        resultadoMvc = mockMvc.perform(get("/api/asistencias/evento/7/count")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        Number total = JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.totalAsistencias");
        assertEquals(11L, total.longValue());
    }
}
