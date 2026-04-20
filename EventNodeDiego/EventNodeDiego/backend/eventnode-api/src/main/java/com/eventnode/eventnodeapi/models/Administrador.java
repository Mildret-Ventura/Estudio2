package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

/** Extensión de usuario con bandera de administrador principal. Tabla {@code administradores}. */
@Entity
@Table(name = "administradores")
public class Administrador implements Persistable<Integer> {

    @Transient
    private boolean isNew = true;

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "es_principal")
    private Boolean esPrincipal;

    public Administrador() {
    }

    public Administrador(Integer idUsuario, Usuario usuario, Boolean esPrincipal) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.esPrincipal = esPrincipal;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }

    @Override
    public Integer getId() {
        return idUsuario;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}

