package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventoTest {

    @Test
    void gettersYSetters() {
        Evento e = new Evento();
        Categoria c = new Categoria();
        c.setIdCategoria(1);
        LocalDateTime ini = LocalDateTime.now();
        e.setIdEvento(10);
        e.setNombre("E");
        e.setDescripcion("D");
        e.setUbicacion("U");
        e.setCapacidadMaxima(50);
        e.setFechaInicio(ini);
        e.setFechaFin(ini.plusHours(3));
        e.setTiempoCancelacionHoras(2);
        e.setTiempoToleranciaMinutos(15);
        e.setBanner("b");
        e.setEstado("ACTIVO");
        e.setCategoria(c);
        e.setCreadoPor(1);
        e.setFechaCreacion(ini);
        assertEquals(10, e.getIdEvento());
        assertEquals(c, e.getCategoria());
    }
}
