package com.eventnode.eventnodeapi.dtos;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoCreateRequestTest {

    @Test
    void valido() {
        EventoCreateRequest r = new EventoCreateRequest();
        r.setNombre("Ev");
        r.setUbicacion("U");
        r.setCapacidadMaxima(10);
        r.setIdCategoria(1);
        r.setTiempoCancelacionHoras(2);
        LocalDateTime ini = LocalDateTime.now().plusDays(1);
        LocalDateTime fin = ini.plusHours(2);
        r.setFechaInicio(ini);
        r.setFechaFin(fin);
        r.setTiempoToleranciaMinutos(0);
        r.setDescripcion("D");
        r.setIdCreador(1);
        r.setOrganizadores(List.of(1, 2));
        r.setBanner("b64");
        assertTrue(DtoValidatorHolder.VALIDATOR.validate(r).isEmpty());
    }

    @Test
    void capacidadNoPositivaFalla() {
        EventoCreateRequest r = new EventoCreateRequest();
        r.setNombre("Ev");
        r.setUbicacion("U");
        r.setCapacidadMaxima(0);
        r.setIdCategoria(1);
        r.setTiempoCancelacionHoras(1);
        LocalDateTime ini = LocalDateTime.now().plusDays(1);
        r.setFechaInicio(ini);
        r.setFechaFin(ini.plusHours(1));
        r.setTiempoToleranciaMinutos(0);
        r.setDescripcion("D");
        r.setIdCreador(1);
        assertFalse(DtoValidatorHolder.VALIDATOR.validate(r).isEmpty());
    }
}
