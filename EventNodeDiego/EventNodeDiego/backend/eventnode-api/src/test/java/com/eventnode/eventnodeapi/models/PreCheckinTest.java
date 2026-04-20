package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreCheckinTest {

    @Test
    void gettersYSetters() {
        PreCheckin p = new PreCheckin();
        LocalDateTime t = LocalDateTime.now();
        p.setIdPrecheckin(1);
        p.setIdUsuario(2);
        p.setIdEvento(3);
        p.setFechaRegistro(t);
        p.setEstado("ACTIVO");
        assertEquals(1, p.getIdPrecheckin());
        assertEquals("ACTIVO", p.getEstado());
    }
}
