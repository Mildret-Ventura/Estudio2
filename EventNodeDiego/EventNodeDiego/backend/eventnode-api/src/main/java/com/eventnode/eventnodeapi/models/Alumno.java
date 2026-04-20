package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;

/** Datos académicos del estudiante; PK compartida con {@link Usuario} ({@code id_usuario}). Tabla {@code alumnos}. */
@Entity
@Table(name = "alumnos")
public class Alumno implements Persistable<Integer> {

    @Transient
    private boolean isNew = true;

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "matricula", nullable = false, unique = true, length = 20)
    private String matricula;

    @Column(name = "fecha_nac", nullable = false)
    private LocalDate fechaNac;

    @Column(name = "edad", nullable = false)
    private Integer edad;

    @Column(name = "sexo", nullable = false, length = 20)
    private String sexo;

    @Column(name = "cuatrimestre", nullable = false)
    private Integer cuatrimestre;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    public Alumno() {
    }

    public Alumno(Integer idUsuario, String matricula, LocalDate fechaNac,
                  Integer edad, String sexo, Integer cuatrimestre, Usuario usuario) {
        this.idUsuario = idUsuario;
        this.matricula = matricula;
        this.fechaNac = fechaNac;
        this.edad = edad;
        this.sexo = sexo;
        this.cuatrimestre = cuatrimestre;
        this.usuario = usuario;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public LocalDate getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(LocalDate fechaNac) {
        this.fechaNac = fechaNac;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Integer getCuatrimestre() {
        return cuatrimestre;
    }

    public void setCuatrimestre(Integer cuatrimestre) {
        this.cuatrimestre = cuatrimestre;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

