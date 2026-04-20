package com.eventnode.eventnodeapi.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRequestTest {

    @Test
    void valido() {
        LoginRequest r = new LoginRequest();
        r.setCorreo("user@test.com");
        r.setPassword("secret");
        assertTrue(DtoValidatorHolder.VALIDATOR.validate(r).isEmpty());
    }

    @Test
    void correoInvalido() {
        LoginRequest r = new LoginRequest();
        r.setCorreo("bad");
        r.setPassword("x");
        assertFalse(DtoValidatorHolder.VALIDATOR.validate(r).isEmpty());
    }
}
