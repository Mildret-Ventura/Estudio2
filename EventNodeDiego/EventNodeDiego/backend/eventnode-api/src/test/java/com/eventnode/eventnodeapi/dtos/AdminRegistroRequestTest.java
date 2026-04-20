package com.eventnode.eventnodeapi.dtos;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminRegistroRequestTest {

    @Test
    void validoSinViolaciones() {
        AdminRegistroRequest r = new AdminRegistroRequest();
        r.setNombre("Ana");
        r.setApellidoPaterno("López");
        r.setApellidoMaterno("Ruiz");
        r.setCorreo("ana@test.com");
        r.setPassword("Password1");
        r.setIdSolicitante(1);
        assertTrue(DtoValidatorHolder.VALIDATOR.validate(r).isEmpty());
    }

    @Test
    void nombreVacioFalla() {
        AdminRegistroRequest r = new AdminRegistroRequest();
        r.setApellidoPaterno("L");
        r.setCorreo("a@b.com");
        r.setPassword("Password1");
        r.setIdSolicitante(1);
        Set<ConstraintViolation<AdminRegistroRequest>> v = DtoValidatorHolder.VALIDATOR.validate(r);
        assertEquals(1, v.size());
    }

    @Test
    void passwordCortaFalla() {
        AdminRegistroRequest r = new AdminRegistroRequest();
        r.setNombre("A");
        r.setApellidoPaterno("B");
        r.setCorreo("a@b.com");
        r.setPassword("short");
        r.setIdSolicitante(1);
        assertTrue(DtoValidatorHolder.VALIDATOR.validate(r).stream()
                .anyMatch(x -> x.getPropertyPath().toString().contains("password")));
    }
}
