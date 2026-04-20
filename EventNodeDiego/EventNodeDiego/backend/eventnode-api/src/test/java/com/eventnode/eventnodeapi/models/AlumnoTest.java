package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AlumnoTest {

    @Test
    void constructorParametrizadoYPersistable() {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        Alumno a = new Alumno(1, "M1", LocalDate.of(2000, 1, 1), 20, "M", 4, u);
        assertEquals(1, a.getId());
        assertEquals("M1", a.getMatricula());
        assertEquals(u, a.getUsuario());
    }

    @Test
    void isNewYMarkNotNew() {
        Alumno a = new Alumno();
        a.setIdUsuario(5);
        assertTrue(a.isNew());
        a.markNotNew();
        assertFalse(a.isNew());
    }
}
