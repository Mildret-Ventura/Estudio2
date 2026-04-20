package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.dtos.AlumnoActualizarRequest;
import com.eventnode.eventnodeapi.dtos.AlumnoRegistroRequest;
import com.eventnode.eventnodeapi.services.AlumnoService;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AlumnoControllerTest {

    private static final String RUTA_REGISTRO = "/api/alumnos/registro";
    private static final String RUTA_ACTUALIZAR = "/api/alumnos/1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlumnoService alumnoService;

    /** Datos de entrada válidos para registro */
    private AlumnoRegistroRequest datosRegistroAlumno;
    /** Datos de entrada para actualización */
    private AlumnoActualizarRequest datosActualizarAlumno;
    /** JSON enviado al endpoint */
    private String cuerpoSolicitudJson;
    /** Resultado HTTP de la última invocación */
    private MvcResult resultadoMvc;
    /** Código de estado HTTP obtenido */
    private int codigoHttpObtenido;
    /** Fragmento de respuesta para validar */
    private String cuerpoRespuesta;

    @BeforeAll
    static void inicializarClase() {
        // Reservado para constantes globales de prueba (ya definidas arriba)
    }

    @BeforeEach
    void reiniciarVariables() {
        Mockito.reset(alumnoService);

        datosRegistroAlumno = new AlumnoRegistroRequest();
        datosRegistroAlumno.setNombre("Juan");
        datosRegistroAlumno.setApellidoPaterno("Pérez");
        datosRegistroAlumno.setApellidoMaterno("López");
        datosRegistroAlumno.setMatricula("A00000001");
        datosRegistroAlumno.setCorreo("juan@test.com");
        datosRegistroAlumno.setPassword("Abcd1234!");
        datosRegistroAlumno.setFechaNacimiento(LocalDate.now().minusYears(20));
        datosRegistroAlumno.setSexo("M");
        datosRegistroAlumno.setCuatrimestre(4);

        datosActualizarAlumno = new AlumnoActualizarRequest();
        datosActualizarAlumno.setNombre("Juan");
        datosActualizarAlumno.setApellidoPaterno("Pérez");
        datosActualizarAlumno.setCorreo("juan@test.com");
        datosActualizarAlumno.setSexo("M");
        datosActualizarAlumno.setCuatrimestre(4);
        datosActualizarAlumno.setEdad(21);

        cuerpoSolicitudJson = null;
        resultadoMvc = null;
        codigoHttpObtenido = 0;
        cuerpoRespuesta = null;
    }

    @Test
    void registrarAlumnoDatosValidosRetornaCreadoTest() throws Exception {
        doNothing().when(alumnoService).registrarAlumno(any(AlumnoRegistroRequest.class));
        cuerpoSolicitudJson = objectMapper.writeValueAsString(datosRegistroAlumno);

        resultadoMvc = mockMvc.perform(post(RUTA_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSolicitudJson))
                .andReturn();

        codigoHttpObtenido = resultadoMvc.getResponse().getStatus();
        cuerpoRespuesta = resultadoMvc.getResponse().getContentAsString();

        assertEquals(201, codigoHttpObtenido);
        assertEquals("Cuenta creada con éxito", JsonPath.read(cuerpoRespuesta, "$.mensaje"));
    }

    @Test
    void registrarAlumnoCorreoInvalidoRetornaValidacionTest() throws Exception {
        datosRegistroAlumno.setCorreo("correo-no-valido");
        cuerpoSolicitudJson = objectMapper.writeValueAsString(datosRegistroAlumno);

        resultadoMvc = mockMvc.perform(post(RUTA_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSolicitudJson))
                .andReturn();

        codigoHttpObtenido = resultadoMvc.getResponse().getStatus();
        assertEquals(400, codigoHttpObtenido);
        assertFalse(resultadoMvc.getResponse().getContentAsString().isEmpty());
    }

    @Test
    void registrarAlumnoNombreVacioRetornaValidacionTest() throws Exception {
        datosRegistroAlumno.setNombre("");
        cuerpoSolicitudJson = objectMapper.writeValueAsString(datosRegistroAlumno);

        resultadoMvc = mockMvc.perform(post(RUTA_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSolicitudJson))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(resultadoMvc.getResponse().getContentAsString().contains("mensaje"));
    }

    @Test
    void registrarAlumnoServicioEstadoInvalidoRetornaBadRequestTest() throws Exception {
        doThrow(new IllegalStateException("Correo duplicado"))
                .when(alumnoService).registrarAlumno(any(AlumnoRegistroRequest.class));
        cuerpoSolicitudJson = objectMapper.writeValueAsString(datosRegistroAlumno);

        resultadoMvc = mockMvc.perform(post(RUTA_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSolicitudJson))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertEquals("Correo duplicado", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void registrarAlumnoJsonCuatrimestreInvalidoRetornaMensajeEspecificoTest() throws Exception {
        String json = "{\"nombre\":\"J\",\"apellidoPaterno\":\"P\",\"apellidoMaterno\":\"M\",\"matricula\":\"A1\",\"correo\":\"a@b.com\",\"password\":\"Abcd1234!\",\"fechaNacimiento\":\"2000-01-01\",\"sexo\":\"M\",\"cuatrimestre\":\"no-es-numero\"}";
        resultadoMvc = mockMvc.perform(post(RUTA_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        cuerpoRespuesta = resultadoMvc.getResponse().getContentAsString();
        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(cuerpoRespuesta.contains("cuatrimestre"));
    }

    @Test
    void registrarAlumnoFechaNacimientoFormatoInvalidoRetornaMensajeFechaTest() throws Exception {
        String json = "{\"nombre\":\"J\",\"apellidoPaterno\":\"P\",\"apellidoMaterno\":\"M\",\"matricula\":\"A1\",\"correo\":\"a@b.com\",\"password\":\"Abcd1234!\",\"fechaNacimiento\":\"31/12/2000\",\"sexo\":\"M\",\"cuatrimestre\":1}";
        resultadoMvc = mockMvc.perform(post(RUTA_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(resultadoMvc.getResponse().getContentAsString().contains("fecha"));
    }

    @Test
    void registrarAlumnoJsonMalformadoRetornaErrorFormatoTest() throws Exception {
        resultadoMvc = mockMvc.perform(post(RUTA_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ mal json"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(resultadoMvc.getResponse().getContentAsString().contains("mensaje"));
    }

    @Test
    void actualizarAlumnoDatosValidosRetornaOkTest() throws Exception {
        doNothing().when(alumnoService).actualizarAlumno(eq(1), any(AlumnoActualizarRequest.class));
        cuerpoSolicitudJson = objectMapper.writeValueAsString(datosActualizarAlumno);

        resultadoMvc = mockMvc.perform(put(RUTA_ACTUALIZAR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSolicitudJson))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("Alumno actualizado con éxito", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void actualizarAlumnoArgumentoInvalidoRetornaBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("Alumno no existe"))
                .when(alumnoService).actualizarAlumno(eq(1), any(AlumnoActualizarRequest.class));
        cuerpoSolicitudJson = objectMapper.writeValueAsString(datosActualizarAlumno);

        resultadoMvc = mockMvc.perform(put(RUTA_ACTUALIZAR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSolicitudJson))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertEquals("Alumno no existe", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void actualizarAlumnoCorreoInvalidoRetornaValidacionTest() throws Exception {
        datosActualizarAlumno.setCorreo("x");
        cuerpoSolicitudJson = objectMapper.writeValueAsString(datosActualizarAlumno);

        resultadoMvc = mockMvc.perform(put(RUTA_ACTUALIZAR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSolicitudJson))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }
}
