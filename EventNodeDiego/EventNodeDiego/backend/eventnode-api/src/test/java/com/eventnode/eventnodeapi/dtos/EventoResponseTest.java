package com.eventnode.eventnodeapi.dtos;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventoResponseTest {

    @Test
    void constructorCompletoYSetters() {
        LocalDateTime ini = LocalDateTime.now();
        LocalDateTime fin = ini.plusHours(2);
        EventoResponse r = new EventoResponse(1, "ban", "nom", "ubi", 10, 1, ini, fin, 5, "desc", "ACTIVO", 2, "Cat", 3L);
        assertEquals(1, r.getIdEvento());
        assertEquals("ban", r.getBanner());
        assertEquals(3L, r.getInscritos());

        r.setEstado("CANCELADO");
        assertEquals("CANCELADO", r.getEstado());
    }
}
