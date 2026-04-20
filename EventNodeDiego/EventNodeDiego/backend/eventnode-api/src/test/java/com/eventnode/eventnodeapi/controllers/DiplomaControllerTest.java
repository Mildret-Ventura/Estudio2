package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.services.DiplomaService;
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

import java.util.ArrayList;
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
class DiplomaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiplomaService diplomaService;

    private Map<String, Object> cuerpoCrearDiploma;
    private String jsonSolicitud;
    private MvcResult resultadoMvc;

    @BeforeAll
    static void inicializarClase() {
        // Sin configuración estática
    }

    @BeforeEach
    void reiniciarVariables() {
        Mockito.reset(diplomaService);

        cuerpoCrearDiploma = new HashMap<>();
        cuerpoCrearDiploma.put("idEvento", 1);
        cuerpoCrearDiploma.put("firma", "Firma");
        cuerpoCrearDiploma.put("diseno", "Clásico");
        cuerpoCrearDiploma.put("plantillaPdf", "contenido-pdf");
        cuerpoCrearDiploma.put("firmaImagen", "contenido-img");

        jsonSolicitud = null;
        resultadoMvc = null;
    }

    private static List<Map<String, Object>> listaEjemplo() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> m = new HashMap<>();
        m.put("idDiploma", 1);
        list.add(m);
        return list;
    }

    @Test
    void listarDiplomasExitosoRetornaOkTest() throws Exception {
        when(diplomaService.listarDiplomas()).thenReturn(listaEjemplo());

        resultadoMvc = mockMvc.perform(get("/api/diplomas/")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        Number idDiploma = JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$[0].idDiploma");
        assertEquals(1, idDiploma.intValue());
    }

    @Test
    void listarDiplomasErrorInternoRetorna500Test() throws Exception {
        when(diplomaService.listarDiplomas()).thenThrow(new RuntimeException("fallo"));

        resultadoMvc = mockMvc.perform(get("/api/diplomas/")).andReturn();

        assertEquals(500, resultadoMvc.getResponse().getStatus());
        assertEquals("Error interno", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void crearDiplomaDatosCompletosRetornaCreadoTest() throws Exception {
        doNothing().when(diplomaService).crearDiploma(anyInt(), anyString(), anyString(), anyString(), anyString());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoCrearDiploma);

        resultadoMvc = mockMvc.perform(post("/api/diplomas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(201, resultadoMvc.getResponse().getStatus());
        assertTrue(JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje")
                .toString().contains("exitosamente"));
    }

    @Test
    void crearDiplomaSinIdEventoRetornaBadRequestTest() throws Exception {
        cuerpoCrearDiploma.remove("idEvento");
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoCrearDiploma);

        resultadoMvc = mockMvc.perform(post("/api/diplomas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje")
                .toString().contains("idEvento"));
    }

    @Test
    void crearDiplomaPlantillaVaciaRetornaBadRequestTest() throws Exception {
        cuerpoCrearDiploma.put("plantillaPdf", "   ");
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoCrearDiploma);

        resultadoMvc = mockMvc.perform(post("/api/diplomas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertFalse(resultadoMvc.getResponse().getContentAsString().isBlank());
    }

    @Test
    void crearDiplomaSinFirmaImagenRetornaBadRequestTest() throws Exception {
        cuerpoCrearDiploma.remove("firmaImagen");
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoCrearDiploma);

        resultadoMvc = mockMvc.perform(post("/api/diplomas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void crearDiplomaServicioArgumentoInvalidoRetornaBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("Evento inválido"))
                .when(diplomaService).crearDiploma(anyInt(), anyString(), anyString(), anyString(), anyString());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoCrearDiploma);

        resultadoMvc = mockMvc.perform(post("/api/diplomas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void crearDiplomaServicioEstadoInvalidoRetornaConflictoTest() throws Exception {
        doThrow(new IllegalStateException("Duplicado"))
                .when(diplomaService).crearDiploma(anyInt(), anyString(), anyString(), anyString(), anyString());
        jsonSolicitud = objectMapper.writeValueAsString(cuerpoCrearDiploma);

        resultadoMvc = mockMvc.perform(post("/api/diplomas/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andReturn();

        assertEquals(409, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void obtenerDiplomaPorIdRetornaOkTest() throws Exception {
        when(diplomaService.obtenerDiploma(1)).thenReturn(Map.of("idDiploma", 1, "nombre", "D1"));

        resultadoMvc = mockMvc.perform(get("/api/diplomas/1")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("D1", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.nombre"));
    }

    @Test
    void obtenerDiplomaNoExisteRetornaBadRequestTest() throws Exception {
        when(diplomaService.obtenerDiploma(99)).thenThrow(new IllegalArgumentException("No encontrado"));

        resultadoMvc = mockMvc.perform(get("/api/diplomas/99")).andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void emitirDiplomasRetornaTotalTest() throws Exception {
        when(diplomaService.emitirDiplomas(2)).thenReturn(Map.of("totalEnviados", 4L, "totalErrores", 0L));

        resultadoMvc = mockMvc.perform(post("/api/diplomas/2/emitir")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        Number n = JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.totalEmitidos");
        assertEquals(4L, n.longValue());
    }

    @Test
    void actualizarDiplomaRetornaOkTest() throws Exception {
        doNothing().when(diplomaService).actualizarDiploma(anyInt(), any(), any(), any(), any());

        resultadoMvc = mockMvc.perform(put("/api/diplomas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void eliminarDiplomaRetornaOkTest() throws Exception {
        doNothing().when(diplomaService).eliminarDiploma(3);

        resultadoMvc = mockMvc.perform(delete("/api/diplomas/3")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void listarDiplomasEstudianteRetornaOkTest() throws Exception {
        when(diplomaService.listarDiplomasEstudiante(5)).thenReturn(listaEjemplo());

        resultadoMvc = mockMvc.perform(get("/api/diplomas/estudiante/5")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void descargarDiplomaPdfRetornaOkTest() throws Exception {
        when(diplomaService.generarDiplomaPdf(1, 2)).thenReturn(new byte[]{10, 20, 30});

        resultadoMvc = mockMvc.perform(get("/api/diplomas/1/descargar/2")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals(3, resultadoMvc.getResponse().getContentAsByteArray().length);
    }

    @Test
    void descargarDiplomaErrorArgumentoRetornaBadRequestTest() throws Exception {
        when(diplomaService.generarDiplomaPdf(1, 2)).thenThrow(new IllegalArgumentException("Sin permiso"));

        resultadoMvc = mockMvc.perform(get("/api/diplomas/1/descargar/2")).andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }
}
