package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Registro de asistencia en evento (método QR/MANUAL, estado PENDIENTE/ASISTIDO). Tabla {@code asistencias}. */
@Entity
@Table(name = "asistencias")
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Integer idAsistencia;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_evento", nullable = false)
    private Integer idEvento;

    @Column(name = "fecha_checkin")
    private LocalDateTime fechaCheckin;

    @Column(name = "metodo", nullable = false, columnDefinition = "ENUM('QR','MANUAL')")
    private String metodo;

    @Column(name = "estado", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDIENTE'")
    private String estado = "PENDIENTE";

    public Asistencia() {
    }

    public Integer getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(Integer idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Integer idEvento) {
        this.idEvento = idEvento;
    }

    public LocalDateTime getFechaCheckin() {
        return fechaCheckin;
    }

    public void setFechaCheckin(LocalDateTime fechaCheckin) {
        this.fechaCheckin = fechaCheckin;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
