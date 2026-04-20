package com.eventnode.eventnodeapi.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdministradorTest {

    @Test
    void constructorYRelacion() {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        Administrador ad = new Administrador(1, u, true);
        assertEquals(1, ad.getId());
        assertTrue(ad.getEsPrincipal());
        assertEquals(u, ad.getUsuario());
    }

    @Test
    void isNewYMarkNotNew() {
        Administrador ad = new Administrador();
        ad.setIdUsuario(2);
        assertTrue(ad.isNew());
        ad.markNotNew();
        assertFalse(ad.isNew());
    }
}
