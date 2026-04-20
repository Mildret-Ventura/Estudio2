package com.eventnode.eventnodeapi.dtos;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EventoUpdateRequestTest {

    @Test
    void gettersYSetters() {
        EventoUpdateRequest r = new EventoUpdateRequest();
        LocalDateTime t = LocalDateTime.now();
        r.setBanner("b");
        r.setNombre("n");
        r.setUbicacion("u");
        r.setCapacidadMaxima(5);
        r.setIdCategoria(2);
        r.setTiempoCancelacionHoras(3);
        r.setFechaInicio(t);
        r.setFechaFin(t.plusHours(1));
        r.setTiempoToleranciaMinutos(10);
        r.setDescripcion("d");
        r.setIdOrganizador(9);
        assertEquals("b", r.getBanner());
        assertEquals("n", r.getNombre());
        assertEquals("u", r.getUbicacion());
        assertEquals(5, r.getCapacidadMaxima());
        assertEquals(2, r.getIdCategoria());
        assertEquals(3, r.getTiempoCancelacionHoras());
        assertEquals(t, r.getFechaInicio());
        assertEquals(t.plusHours(1), r.getFechaFin());
        assertEquals(10, r.getTiempoToleranciaMinutos());
        assertEquals("d", r.getDescripcion());
        assertEquals(9, r.getIdOrganizador());
    }

    @Test
    void constructorPorDefecto() {
        assertNull(new EventoUpdateRequest().getNombre());
    }
}
