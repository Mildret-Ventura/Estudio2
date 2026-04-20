package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiplomaTest {

    @Test
    void gettersYSetters() {
        Diploma d = new Diploma();
        LocalDateTime t = LocalDateTime.now();
        d.setIdDiploma(1);
        d.setIdEvento(2);
        d.setNombreEvento("Grad");
        d.setFirma("F");
        d.setDiseno("D");
        d.setPlantillaPdf("pdf");
        d.setFirmaImagen("img");
        d.setFechaCreacion(t);
        d.setEstado("ACTIVO");
        assertEquals(1, d.getIdDiploma());
        assertEquals("pdf", d.getPlantillaPdf());
    }
}
