package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrganizadorTest {

    @Test
    void gettersYSetters() {
        Organizador o = new Organizador();
        o.setIdOrganizador(1);
        o.setNombre("Org");
        o.setDescripcion("Desc");
        o.setCorreo("o@test.com");
        assertEquals("Org", o.getNombre());
        assertEquals("o@test.com", o.getCorreo());
    }
}
