package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiplomaEmitidoTest {

    @Test
    void gettersYSetters() {
        DiplomaEmitido e = new DiplomaEmitido();
        LocalDateTime t = LocalDateTime.now();
        e.setIdEmitido(1);
        e.setIdDiploma(2);
        e.setIdUsuario(3);
        e.setFechaEnvio(t);
        e.setEstadoEnvio("ENVIADO");
        assertEquals("ENVIADO", e.getEstadoEnvio());
        assertEquals(t, e.getFechaEnvio());
    }
}
