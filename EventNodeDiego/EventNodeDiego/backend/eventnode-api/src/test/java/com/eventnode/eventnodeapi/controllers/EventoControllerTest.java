package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.dtos.EventoCreateRequest;
import com.eventnode.eventnodeapi.dtos.EventoUpdateRequest;
import com.eventnode.eventnodeapi.models.Categoria;
import com.eventnode.eventnodeapi.models.Evento;
import com.eventnode.eventnodeapi.models.Organizador;
import com.eventnode.eventnodeapi.repositories.AsistenciaRepository;
import com.eventnode.eventnodeapi.repositories.CategoriaRepository;
import com.eventnode.eventnodeapi.repositories.OrganizadorRepository;
import com.eventnode.eventnodeapi.repositories.PreCheckinRepository;
import com.eventnode.eventnodeapi.services.EventoService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventoService eventoService;

    @MockBean
    private CategoriaRepository categoriaRepository;

    @MockBean
    private OrganizadorRepository organizadorRepository;

    @MockBean
    private PreCheckinRepository preCheckinRepository;

    @MockBean
    private AsistenciaRepository asistenciaRepository;

    private Evento eventoEjemplo;
    private EventoCreateRequest solicitudCrearEvento;
    private String jsonCrearEvento;
    private MvcResult resultadoMvc;

    @BeforeAll
    static void inicializarClase() {
        // Sin estado compartido entre hilos
    }

    @BeforeEach
    void reiniciarVariables() {
        Mockito.reset(eventoService, categoriaRepository, organizadorRepository, preCheckinRepository, asistenciaRepository);

        Categoria cat = new Categoria();
        cat.setIdCategoria(1);
        cat.setNombre("Conferencias");

        eventoEjemplo = new Evento();
        eventoEjemplo.setIdEvento(9);
        eventoEjemplo.setNombre("Evento Demo");
        eventoEjemplo.setDescripcion("Desc");
        eventoEjemplo.setUbicacion("Aula 1");
        eventoEjemplo.setCapacidadMaxima(50);
        eventoEjemplo.setTiempoCancelacionHoras(2);
        LocalDateTime ini = LocalDateTime.now().plusDays(1);
        eventoEjemplo.setFechaInicio(ini);
        eventoEjemplo.setFechaFin(ini.plusHours(3));
        eventoEjemplo.setTiempoToleranciaMinutos(10);
        eventoEjemplo.setEstado("ACTIVO");
        eventoEjemplo.setCategoria(cat);
        eventoEjemplo.setCreadoPor(1);
        eventoEjemplo.setFechaCreacion(LocalDateTime.now());

        solicitudCrearEvento = new EventoCreateRequest();
        solicitudCrearEvento.setNombre("Nuevo");
        solicitudCrearEvento.setUbicacion("U");
        solicitudCrearEvento.setCapacidadMaxima(20);
        solicitudCrearEvento.setIdCategoria(1);
        solicitudCrearEvento.setTiempoCancelacionHoras(1);
        solicitudCrearEvento.setFechaInicio(ini);
        solicitudCrearEvento.setFechaFin(ini.plusHours(2));
        solicitudCrearEvento.setTiempoToleranciaMinutos(0);
        solicitudCrearEvento.setDescripcion("D");
        solicitudCrearEvento.setIdCreador(1);

        jsonCrearEvento = null;
        resultadoMvc = null;
    }

    @Test
    void consultarEventosConResultadosRetornaListaTest() throws Exception {
        when(eventoService.consultarEventosDisponibles(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(eventoEjemplo));
        when(preCheckinRepository.countByIdEventoAndEstado(anyInt(), anyString())).thenReturn(0L);

        resultadoMvc = mockMvc.perform(get("/api/eventos")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("Evento Demo", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$[0].nombre"));
    }

    @Test
    void consultarEventosListaVaciaRetornaMensajeTest() throws Exception {
        when(eventoService.consultarEventosDisponibles(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        resultadoMvc = mockMvc.perform(get("/api/eventos")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("No se encontraron resultados",
                JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void listarCategoriasRetornaOkTest() throws Exception {
        Categoria c = new Categoria();
        c.setIdCategoria(2);
        c.setNombre("Taller");
        when(categoriaRepository.findAll()).thenReturn(List.of(c));

        resultadoMvc = mockMvc.perform(get("/api/eventos/categorias")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("Taller", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$[0].nombre"));
    }

    @Test
    void buscarOrganizadoresNombreVacioUsaFindAllTest() throws Exception {
        Organizador o = new Organizador();
        o.setIdOrganizador(1);
        o.setNombre("Org");
        o.setCorreo("o@test.com");
        when(organizadorRepository.findAll()).thenReturn(List.of(o));

        resultadoMvc = mockMvc.perform(get("/api/eventos/organizadores")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        verify(organizadorRepository).findAll();
        verify(organizadorRepository, never()).findByNombreContainingIgnoreCase(anyString());
    }

    @Test
    void buscarOrganizadoresConNombreUsaBusquedaTest() throws Exception {
        Organizador o = new Organizador();
        o.setIdOrganizador(3);
        o.setNombre("Filtrado");
        when(organizadorRepository.findByNombreContainingIgnoreCase("fil")).thenReturn(List.of(o));

        resultadoMvc = mockMvc.perform(get("/api/eventos/organizadores").param("nombre", "fil")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        verify(organizadorRepository).findByNombreContainingIgnoreCase("fil");
    }

    @Test
    void crearOrganizadorNombreValidoRetornaCreadoTest() throws Exception {
        Organizador guardado = new Organizador();
        guardado.setIdOrganizador(10);
        guardado.setNombre("Nuevo");
        guardado.setCorreo("n@test.com");
        when(organizadorRepository.save(any(Organizador.class))).thenReturn(guardado);

        resultadoMvc = mockMvc.perform(post("/api/eventos/organizadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\",\"correo\":\"n@test.com\"}"))
                .andReturn();

        assertEquals(201, resultadoMvc.getResponse().getStatus());
        Number idOrg = JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.idOrganizador");
        assertEquals(10, idOrg.intValue());
    }

    @Test
    void crearOrganizadorNombreVacioRetornaBadRequestTest() throws Exception {
        resultadoMvc = mockMvc.perform(post("/api/eventos/organizadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"  \"}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje")
                .toString().contains("obligatorio"));
    }

    @Test
    void actualizarOrganizadorExistenteRetornaOkTest() throws Exception {
        Organizador org = new Organizador();
        org.setIdOrganizador(1);
        org.setNombre("Viejo");
        when(organizadorRepository.findById(1)).thenReturn(Optional.of(org));
        when(organizadorRepository.save(any(Organizador.class))).thenAnswer(inv -> inv.getArgument(0));

        resultadoMvc = mockMvc.perform(put("/api/eventos/organizadores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"NuevoNombre\"}"))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("NuevoNombre", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.nombre"));
    }

    @Test
    void actualizarOrganizadorInexistenteRetornaBadRequestTest() throws Exception {
        when(organizadorRepository.findById(99)).thenReturn(Optional.empty());

        resultadoMvc = mockMvc.perform(put("/api/eventos/organizadores/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X\"}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void eliminarOrganizadorExistenteRetornaOkTest() throws Exception {
        when(organizadorRepository.existsById(1)).thenReturn(true);
        doNothing().when(organizadorRepository).deleteById(1);

        resultadoMvc = mockMvc.perform(delete("/api/eventos/organizadores/1")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void eliminarOrganizadorInexistenteRetornaBadRequestTest() throws Exception {
        when(organizadorRepository.existsById(2)).thenReturn(false);

        resultadoMvc = mockMvc.perform(delete("/api/eventos/organizadores/2")).andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void crearEventoValidoRetornaCreadoTest() throws Exception {
        doNothing().when(eventoService).crearEvento(any(EventoCreateRequest.class));
        jsonCrearEvento = objectMapper.writeValueAsString(solicitudCrearEvento);

        resultadoMvc = mockMvc.perform(post("/api/eventos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCrearEvento))
                .andReturn();

        assertEquals(201, resultadoMvc.getResponse().getStatus());
        assertTrue(JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje")
                .toString().contains("éxito"));
    }

    @Test
    void crearEventoNombreVacioRetornaValidacionTest() throws Exception {
        solicitudCrearEvento.setNombre("");
        jsonCrearEvento = objectMapper.writeValueAsString(solicitudCrearEvento);

        resultadoMvc = mockMvc.perform(post("/api/eventos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCrearEvento))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void crearEventoProhibidoRetorna403Test() throws Exception {
        doThrow(new SecurityException("Sin permiso")).when(eventoService).crearEvento(any(EventoCreateRequest.class));
        jsonCrearEvento = objectMapper.writeValueAsString(solicitudCrearEvento);

        resultadoMvc = mockMvc.perform(post("/api/eventos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCrearEvento))
                .andReturn();

        assertEquals(403, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void crearEventoArgumentoInvalidoRetornaBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("Categoría inválida"))
                .when(eventoService).crearEvento(any(EventoCreateRequest.class));
        jsonCrearEvento = objectMapper.writeValueAsString(solicitudCrearEvento);

        resultadoMvc = mockMvc.perform(post("/api/eventos/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCrearEvento))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void obtenerDetalleEventoRetornaOkTest() throws Exception {
        when(eventoService.consultarEventoPorId(9)).thenReturn(eventoEjemplo);
        when(preCheckinRepository.countByIdEventoAndEstado(9, "ACTIVO")).thenReturn(2L);
        when(asistenciaRepository.countByIdEvento(9)).thenReturn(1L);
        when(organizadorRepository.findByEventoId(9)).thenReturn(Collections.emptyList());

        resultadoMvc = mockMvc.perform(get("/api/eventos/9")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        Number inscritos = JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.inscritos");
        assertEquals(2L, inscritos.longValue());
    }

    @Test
    void actualizarEventoRetornaOkTest() throws Exception {
        doNothing().when(eventoService).actualizarEvento(eq(9), any(EventoUpdateRequest.class));

        resultadoMvc = mockMvc.perform(put("/api/eventos/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void eliminarEventoRetornaOkTest() throws Exception {
        doNothing().when(eventoService).eliminarEvento(9);

        resultadoMvc = mockMvc.perform(delete("/api/eventos/9")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void cancelarEventoRetornaOkTest() throws Exception {
        doNothing().when(eventoService).cancelarEvento(9);

        resultadoMvc = mockMvc.perform(post("/api/eventos/9/cancelar")).andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }
}
