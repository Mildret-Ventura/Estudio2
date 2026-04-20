package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.services.PreCheckinService;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PreCheckinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PreCheckinService preCheckinService;

    private Map<String, Integer> cuerpoInscripcion;
    private String jsonSolicitud;
    private MvcResult resultadoMvc;

    @BeforeAll
    static void inicializarClase() {
        // Nada estático
    }

    @BeforeEach
    void reiniciarVariables() {
        Mockito.reset(preCheckinService);
        cuerpoInscripcion = Map.of("idUsuario", 1, "idEvento", 2);
        jsonSolicitud = null;
        resultadoMvc = null;
    }

    @Test
    void inscribirseDatosCompletosRetornaCreadoTest() throws Exception {
        doNothing().when(preCheckinService).inscribirse(anyInt(), anyInt());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoInscripcion);

        resultadoMvc = mockMvc.perform(post("/api/precheckin/inscribirse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(201, resultadoMvc.getResponse().getStatus());
        assertEquals("Inscripción exitosa",
                JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void inscribirseFaltaIdUsuarioRetornaBadRequestTest() throws Exception {
        jsonSolicitud = objectMapper.writeValueAsString(Map.of("idEvento", 2));

        resultadoMvc = mockMvc.perform(post("/api/precheckin/inscribirse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(resultadoMvc.getResponse().getContentAsString().contains("requeridos"));
    }

    @Test
    void inscribirseCuerpoVacioRetornaBadRequestTest() throws Exception {
        resultadoMvc = mockMvc.perform(post("/api/precheckin/inscribirse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void inscribirseArgumentoInvalidoRetornaBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("Cupos llenos"))
                .when(preCheckinService).inscribirse(anyInt(), anyInt());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoInscripcion);

        resultadoMvc = mockMvc.perform(post("/api/precheckin/inscribirse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void inscribirseEstadoInvalidoRetornaConflictoTest() throws Exception {
        doThrow(new IllegalStateException("Ya inscrito"))
                .when(preCheckinService).inscribirse(anyInt(), anyInt());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoInscripcion);

        resultadoMvc = mockMvc.perform(post("/api/precheckin/inscribirse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(409, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void cancelarInscripcionExitosaRetornaOkTest() throws Exception {
        doNothing().when(preCheckinService).cancelarInscripcion(anyInt(), anyInt());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoInscripcion);

        resultadoMvc = mockMvc.perform(post("/api/precheckin/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void cancelarInscripcionConflictoRetorna409Test() throws Exception {
        doThrow(new IllegalStateException("No puede cancelar"))
                .when(preCheckinService).cancelarInscripcion(anyInt(), anyInt());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoInscripcion);

        resultadoMvc = mockMvc.perform(post("/api/precheckin/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(409, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void listarInscritosPorEventoRetornaOkTest() throws Exception {
        when(preCheckinService.listarInscritos(3)).thenReturn(List.of(Map.of("idUsuario", 1)));

        resultadoMvc = mockMvc.perform(get("/api/precheckin/evento/3")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void listarInscritosErrorInternoRetorna500Test() throws Exception {
        when(preCheckinService.listarInscritos(8)).thenThrow(new RuntimeException("db"));

        resultadoMvc = mockMvc.perform(get("/api/precheckin/evento/8")).andReturn();

        assertEquals(500, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void listarEventosPorUsuarioRetornaOkTest() throws Exception {
        when(preCheckinService.listarEventosInscritos(4)).thenReturn(List.of());

        resultadoMvc = mockMvc.perform(get("/api/precheckin/usuario/4")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertTrue(resultadoMvc.getResponse().getContentAsString().trim().equals("[]"));
    }

    @Test
    void contarInscritosRetornaTotalTest() throws Exception {
        when(preCheckinService.contarInscritos(5)).thenReturn(8L);

        resultadoMvc = mockMvc.perform(get("/api/precheckin/evento/5/count")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        Number n = JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.totalInscritos");
        assertEquals(8L, n.longValue());
    }
}
