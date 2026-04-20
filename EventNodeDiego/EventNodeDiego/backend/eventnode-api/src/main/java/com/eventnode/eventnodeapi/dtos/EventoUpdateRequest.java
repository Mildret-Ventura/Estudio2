package com.eventnode.eventnodeapi.dtos;

import java.time.LocalDateTime;

/** Actualización parcial de evento: todos los campos opcionales; el servicio valida coherencia de fechas. */
public class EventoUpdateRequest {

    private String banner;
    private String nombre;
    private String ubicacion;
    private Integer capacidadMaxima;
    private Integer idCategoria;
    private Integer tiempoCancelacionHoras;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer tiempoToleranciaMinutos;
    private String descripcion;
    private Integer idOrganizador;

    public EventoUpdateRequest() {
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Integer getTiempoCancelacionHoras() {
        return tiempoCancelacionHoras;
    }

    public void setTiempoCancelacionHoras(Integer tiempoCancelacionHoras) {
        this.tiempoCancelacionHoras = tiempoCancelacionHoras;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getTiempoToleranciaMinutos() {
        return tiempoToleranciaMinutos;
    }

    public void setTiempoToleranciaMinutos(Integer tiempoToleranciaMinutos) {
        this.tiempoToleranciaMinutos = tiempoToleranciaMinutos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIdOrganizador() {
        return idOrganizador;
    }

    public void setIdOrganizador(Integer idOrganizador) {
        this.idOrganizador = idOrganizador;
    }
}

