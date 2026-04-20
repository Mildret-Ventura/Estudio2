package com.eventnode.eventnodeapi.dtos;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlumnoActualizarRequestTest {

    @Test
    void valido() {
        AlumnoActualizarRequest r = new AlumnoActualizarRequest();
        r.setNombre("Juan");
        r.setApellidoPaterno("P");
        r.setCorreo("juan@test.com");
        r.setSexo("M");
        r.setCuatrimestre(4);
        r.setEdad(20);
        assertTrue(DtoValidatorHolder.VALIDATOR.validate(r).isEmpty());
    }

    @Test
    void correoInvalidoFalla() {
        AlumnoActualizarRequest r = new AlumnoActualizarRequest();
        r.setNombre("Juan");
        r.setApellidoPaterno("P");
        r.setCorreo("no-email");
        r.setSexo("M");
        r.setCuatrimestre(4);
        r.setEdad(20);
        Set<ConstraintViolation<AlumnoActualizarRequest>> v = DtoValidatorHolder.VALIDATOR.validate(r);
        assertFalse(v.isEmpty());
    }
}
