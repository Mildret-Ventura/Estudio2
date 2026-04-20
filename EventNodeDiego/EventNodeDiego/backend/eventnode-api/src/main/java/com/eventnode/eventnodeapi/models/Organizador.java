package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

/** Responsable del evento; relación N:M con eventos vía tabla {@code evento_organizador}. */
@Entity
@Table(name = "organizadores")
public class Organizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_organizador")
    private Integer idOrganizador;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "correo", length = 150)
    private String correo;

    public Organizador() {
    }

    public Integer getIdOrganizador() {
        return idOrganizador;
    }

    public void setIdOrganizador(Integer idOrganizador) {
        this.idOrganizador = idOrganizador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}

