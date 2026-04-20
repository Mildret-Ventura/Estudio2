package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.dtos.LoginRequest;
import com.eventnode.eventnodeapi.dtos.LoginResponse;
import com.eventnode.eventnodeapi.services.AuthService;
import com.eventnode.eventnodeapi.services.PasswordRecoveryService;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private PasswordRecoveryService passwordRecoveryService;

    private LoginRequest datosLogin;
    private String jsonLogin;
    private MvcResult resultadoMvc;

    @BeforeAll
    static void inicializarClase() {
        // Sin estado estático requerido
    }

    @BeforeEach
    void reiniciarVariables() {
        Mockito.reset(authService, passwordRecoveryService);

        datosLogin = new LoginRequest();
        datosLogin.setCorreo("usuario@test.com");
        datosLogin.setPassword("Secret1!");

        jsonLogin = null;
        resultadoMvc = null;
    }

    @Test
    void loginCredencialesValidasRetornaOkYTokenTest() throws Exception {
        LoginResponse respuesta = new LoginResponse("ok", "ALUMNO", 1, "N", "P", null,
                "usuario@test.com", null, null, null, "jwt-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(respuesta);
        jsonLogin = objectMapper.writeValueAsString(datosLogin);

        resultadoMvc = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertEquals("jwt-token", JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.token"));
    }

    @Test
    void loginCorreoVacioRetornaValidacionTest() throws Exception {
        datosLogin.setCorreo("");
        datosLogin.setPassword("x");
        jsonLogin = objectMapper.writeValueAsString(datosLogin);

        resultadoMvc = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void loginCuentaInactivaRetornaProhibidoTest() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new DisabledException("off"));
        jsonLogin = objectMapper.writeValueAsString(datosLogin);

        resultadoMvc = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin))
                .andReturn();

        assertEquals(403, resultadoMvc.getResponse().getStatus());
        assertTrue(JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje")
                .toString().contains("inactiva"));
    }

    @Test
    void loginCuentaBloqueadaRetornaProhibidoTest() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new LockedException("lock"));
        jsonLogin = objectMapper.writeValueAsString(datosLogin);

        resultadoMvc = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin))
                .andReturn();

        assertEquals(403, resultadoMvc.getResponse().getStatus());
        assertFalse(resultadoMvc.getResponse().getContentAsString().isBlank());
    }

    @Test
    void loginCredencialesIncorrectasRetornaNoAutorizadoTest() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("bad"));
        jsonLogin = objectMapper.writeValueAsString(datosLogin);

        resultadoMvc = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin))
                .andReturn();

        assertEquals(401, resultadoMvc.getResponse().getStatus());
        assertEquals("Credenciales incorrectas",
                JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void enviarCodigoCorreoValidoRetornaOkTest() throws Exception {
        doNothing().when(passwordRecoveryService).enviarCodigo(anyString());

        resultadoMvc = mockMvc.perform(post("/api/auth/recuperar/enviar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"  a@test.com  \"}"))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
        assertTrue(JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje")
                .toString().contains("Código"));
    }

    @Test
    void enviarCodigoCorreoVacioRetornaBadRequestTest() throws Exception {
        resultadoMvc = mockMvc.perform(post("/api/auth/recuperar/enviar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"   \"}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertEquals("El correo es obligatorio",
                JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void enviarCodigoCorreoNuloRetornaBadRequestTest() throws Exception {
        resultadoMvc = mockMvc.perform(post("/api/auth/recuperar/enviar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void enviarCodigoServicioRechazaRetornaBadRequestTest() throws Exception {
        doThrow(new IllegalArgumentException("Correo no registrado"))
                .when(passwordRecoveryService).enviarCodigo(anyString());

        resultadoMvc = mockMvc.perform(post("/api/auth/recuperar/enviar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"x@test.com\"}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertEquals("Correo no registrado",
                JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje"));
    }

    @Test
    void verificarCodigoDatosCompletosRetornaOkTest() throws Exception {
        doNothing().when(passwordRecoveryService).verificarCodigo(anyString(), anyString());

        resultadoMvc = mockMvc.perform(post("/api/auth/recuperar/verificar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"a@test.com\",\"codigo\":\"123456\"}"))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void verificarCodigoFaltaCodigoRetornaBadRequestTest() throws Exception {
        resultadoMvc = mockMvc.perform(post("/api/auth/recuperar/verificar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"a@test.com\"}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertTrue(JsonPath.read(resultadoMvc.getResponse().getContentAsString(), "$.mensaje")
                .toString().contains("obligatorios"));
    }

    @Test
    void restablecerPasswordCamposCompletosRetornaOkTest() throws Exception {
        doNothing().when(passwordRecoveryService).restablecerPassword(anyString(), anyString(), anyString());

        resultadoMvc = mockMvc.perform(post("/api/auth/recuperar/restablecer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"a@test.com\",\"codigo\":\"1\",\"nuevaPassword\":\"NuevaPass1\"}"))
                .andReturn();

        assertEquals(200, resultadoMvc.getResponse().getStatus());
    }

    @Test
    void restablecerPasswordFaltaPasswordRetornaBadRequestTest() throws Exception {
        resultadoMvc = mockMvc.perform(post("/api/auth/recuperar/restablecer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"a@test.com\",\"codigo\":\"1\"}"))
                .andReturn();

        assertEquals(400, resultadoMvc.getResponse().getStatus());
        assertFalse(resultadoMvc.getResponse().getContentAsString().isEmpty());
    }
}
