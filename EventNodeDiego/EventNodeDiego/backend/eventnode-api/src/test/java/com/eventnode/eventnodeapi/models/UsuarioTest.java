package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UsuarioTest {

    @Test
    void gettersYSetters() {
        Usuario u = new Usuario();
        Rol r = new Rol(1, "ALUMNO");
        LocalDateTime t = LocalDateTime.now();
        u.setIdUsuario(1);
        u.setNombre("N");
        u.setApellidoPaterno("P");
        u.setApellidoMaterno("M");
        u.setCorreo("c@test.com");
        u.setPassword("x");
        u.setRecoverPassword("rc");
        u.setEstado("ACTIVO");
        u.setIntentosFallidos(0);
        u.setBloqueadoHasta(t);
        u.setRol(r);
        u.setFechaCreacion(t);
        assertEquals(1, u.getIdUsuario());
        assertEquals(r, u.getRol());
        assertEquals(t, u.getBloqueadoHasta());
    }

    @Test
    void constructorVacio() {
        assertNull(new Usuario().getNombre());
    }
}
