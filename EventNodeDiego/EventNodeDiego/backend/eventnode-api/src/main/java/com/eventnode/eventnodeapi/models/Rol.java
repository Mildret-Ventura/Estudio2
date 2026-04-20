package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

/** Catálogo de roles (ALUMNO, ADMINISTRADOR, SUPERADMIN). Tabla {@code roles}. */
@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre", nullable = false, unique = true, length = 30)
    private String nombre;

    public Rol() {
    }

    public Rol(Integer idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

