package com.eventnode.eventnodeapi.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginResponseTest {

    @Test
    void constructorYSetters() {
        LoginResponse r = new LoginResponse("m", "ALUMNO", 1, "N", "P", "M", "c@test.com", "MAT", "F", 5, "tok");
        assertEquals("tok", r.getToken());
        r.setMensaje("ok");
        assertEquals("ok", r.getMensaje());
    }
}
