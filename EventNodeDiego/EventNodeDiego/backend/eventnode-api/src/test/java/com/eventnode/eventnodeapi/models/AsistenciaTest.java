package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsistenciaTest {

    @Test
    void gettersYSettersYEstadoPorDefecto() {
        Asistencia a = new Asistencia();
        LocalDateTime t = LocalDateTime.now();
        a.setIdAsistencia(1);
        a.setIdUsuario(2);
        a.setIdEvento(3);
        a.setFechaCheckin(t);
        a.setMetodo("QR");
        a.setEstado("CONFIRMADO");
        assertEquals("CONFIRMADO", a.getEstado());
        Asistencia b = new Asistencia();
        assertEquals("PENDIENTE", b.getEstado());
    }
}
