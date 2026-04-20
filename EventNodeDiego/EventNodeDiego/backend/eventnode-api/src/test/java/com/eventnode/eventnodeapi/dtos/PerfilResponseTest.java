package com.eventnode.eventnodeapi.dtos;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerfilResponseTest {

    @Test
    void gettersYSetters() {
        PerfilResponse p = new PerfilResponse();
        p.setIdUsuario(1);
        p.setNombre("A");
        p.setApellidoPaterno("B");
        p.setApellidoMaterno("C");
        p.setCorreo("a@b.com");
        p.setEstado("ACTIVO");
        p.setRol("ALUMNO");
        p.setMatricula("M1");
        p.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        p.setEdad(24);
        p.setSexo("M");
        p.setCuatrimestre(3);
        p.setEsPrincipal(true);
        assertEquals(1, p.getIdUsuario());
        assertEquals("M1", p.getMatricula());
        assertTrue(p.getEsPrincipal());
    }
}
