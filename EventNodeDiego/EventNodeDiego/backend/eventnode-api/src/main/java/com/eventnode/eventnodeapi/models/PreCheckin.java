package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Inscripción de un usuario a un evento (estado ACTIVO o CANCELADO). Tabla {@code pre_checkin}. */
@Entity
@Table(name = "pre_checkin")
public class PreCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_precheckin")
    private Integer idPrecheckin;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_evento", nullable = false)
    private Integer idEvento;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('ACTIVO','CANCELADO')")
    private String estado;

    public PreCheckin() {
    }

    public Integer getIdPrecheckin() {
        return idPrecheckin;
    }

    public void setIdPrecheckin(Integer idPrecheckin) {
        this.idPrecheckin = idPrecheckin;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
