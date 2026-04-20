package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoriaTest {

    @Test
    void gettersYSetters() {
        Categoria c = new Categoria();
        c.setIdCategoria(3);
        c.setNombre("Conf");
        assertEquals(3, c.getIdCategoria());
        assertEquals("Conf", c.getNombre());
    }
}
