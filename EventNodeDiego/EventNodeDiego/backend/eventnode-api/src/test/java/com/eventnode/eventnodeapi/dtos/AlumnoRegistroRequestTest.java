package com.eventnode.eventnodeapi.dtos;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlumnoRegistroRequestTest {

    @Test
    void valido() {
        AlumnoRegistroRequest r = new AlumnoRegistroRequest();
        r.setNombre("Juan");
        r.setApellidoPaterno("P");
        r.setApellidoMaterno("M");
        r.setMatricula("A00000001");
        r.setCorreo("juan@test.com");
        r.setPassword("Abcd1234!");
        r.setFechaNacimiento(LocalDate.now().minusYears(18));
        r.setSexo("M");
        r.setCuatrimestre(1);
        assertTrue(DtoValidatorHolder.VALIDATOR.validate(r).isEmpty());
    }

    @Test
    void gettersSetters() {
        AlumnoRegistroRequest r = new AlumnoRegistroRequest();
        r.setNombre("X");
        assertEquals("X", r.getNombre());
    }
}
