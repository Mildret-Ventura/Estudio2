package com.eventnode.eventnodeapi.dtos;

import java.time.LocalDateTime;

/** Vista resumida de evento para listados (incluye conteo de inscritos activos). */
public class EventoResponse {
    private Integer idEvento;
    private String banner;
    private String nombre;
    private String ubicacion;
    private Integer capacidadMaxima;
    private Integer tiempoCancelacionHoras;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer tiempoToleranciaMinutos;
    private String descripcion;
    private String estado;
    private Integer categoriaId;
    private String categoriaNombre;
    private Long inscritos;

    public EventoResponse() {
    }

    public EventoResponse(Integer idEvento, String banner, String nombre, String ubicacion,
                          Integer capacidadMaxima, Integer tiempoCancelacionHoras,
                          LocalDateTime fechaInicio, LocalDateTime fechaFin,
                          Integer tiempoToleranciaMinutos, String descripcion,
                          String estado, Integer categoriaId, String categoriaNombre,
                          Long inscritos) {
        this.idEvento = idEvento;
        this.banner = banner;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidadMaxima = capacidadMaxima;
        this.tiempoCancelacionHoras = tiempoCancelacionHoras;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tiempoToleranciaMinutos = tiempoToleranciaMinutos;
        this.descripcion = descripcion;
        this.estado = estado;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.inscritos = inscritos;
    }

    // Getters and Setters
    public Integer getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Integer idEvento) {
        this.idEvento = idEvento;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public Long getInscritos() {
        return inscritos;
    }

    public void setInscritos(Long inscritos) {
        this.inscritos = inscritos;
    }
}
