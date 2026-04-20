package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Auditoría de envío de diploma a un usuario (ENVIADO/ERROR). Tabla {@code diplomas_emitidos}. */
@Entity
@Table(name = "diplomas_emitidos")
public class DiplomaEmitido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emitido")
    private Integer idEmitido;

    @Column(name = "id_diploma", nullable = false)
    private Integer idDiploma;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "estado_envio", nullable = false, columnDefinition = "ENUM('ENVIADO','ERROR')")
    private String estadoEnvio;

    public DiplomaEmitido() {
    }

    public Integer getIdEmitido() {
        return idEmitido;
    }

    public void setIdEmitido(Integer idEmitido) {
        this.idEmitido = idEmitido;
    }

    public Integer getIdDiploma() {
        return idDiploma;
    }

    public void setIdDiploma(Integer idDiploma) {
        this.idDiploma = idDiploma;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(String estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }
}
