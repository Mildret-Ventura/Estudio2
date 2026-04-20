package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RolTest {

    @Test
    void constructorYSetters() {
        Rol r = new Rol(2, "ADMIN");
        assertEquals("ADMIN", r.getNombre());
        r.setNombre("X");
        r.setIdRol(9);
        assertEquals(9, r.getIdRol());
    }
}
