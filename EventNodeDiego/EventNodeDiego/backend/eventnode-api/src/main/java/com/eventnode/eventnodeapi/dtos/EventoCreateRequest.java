package com.eventnode.eventnodeapi.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/** Payload validado para crear un evento (incluye {@code idCreador} y lista opcional de ids de organizadores). */
public class EventoCreateRequest {

    private String banner;

    @NotBlank(message = "El nombre del evento es obligatorio")
    private String nombre;

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @NotNull(message = "La capacidad máxima es obligatoria")
    @Positive(message = "La capacidad máxima debe ser mayor a cero")
    private Integer capacidadMaxima;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    @NotNull(message = "El tiempo de cancelación es obligatorio")
    @Positive(message = "El tiempo de cancelación debe ser mayor a cero")
    private Integer tiempoCancelacionHoras;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFin;

    @NotNull(message = "El tiempo de tolerancia es obligatorio")
    @PositiveOrZero(message = "El tiempo de tolerancia debe ser mayor o igual a cero")
    private Integer tiempoToleranciaMinutos;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private Integer idOrganizador;

    private List<Integer> organizadores;

    @NotNull(message = "Se debe indicar quién crea el evento")
    private Integer idCreador;

    public EventoCreateRequest() {
    }

    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public Integer getTiempoCancelacionHoras() { return tiempoCancelacionHoras; }
    public void setTiempoCancelacionHoras(Integer tiempoCancelacionHoras) { this.tiempoCancelacionHoras = tiempoCancelacionHoras; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public Integer getTiempoToleranciaMinutos() { return tiempoToleranciaMinutos; }
    public void setTiempoToleranciaMinutos(Integer tiempoToleranciaMinutos) { this.tiempoToleranciaMinutos = tiempoToleranciaMinutos; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getIdOrganizador() { return idOrganizador; }
    public void setIdOrganizador(Integer idOrganizador) { this.idOrganizador = idOrganizador; }

    public List<Integer> getOrganizadores() { return organizadores; }
    public void setOrganizadores(List<Integer> organizadores) { this.organizadores = organizadores; }

    public Integer getIdCreador() { return idCreador; }
    public void setIdCreador(Integer idCreador) { this.idCreador = idCreador; }
}
