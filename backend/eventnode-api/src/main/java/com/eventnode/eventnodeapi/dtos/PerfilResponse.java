package com.eventnode.eventnodeapi.dtos;

import java.time.LocalDate;

public class PerfilResponse {

    // Datos de usuario (comunes)
    private Integer idUsuario;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correo;
    private String estado;
    private String rol;

    // Datos de alumno (si aplica)
    private String matricula;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private String sexo;
    private Integer cuatrimestre;

    // Datos de administrador (si aplica)
    private Boolean esPrincipal;

    public PerfilResponse() {
    }

    // --- Getters y Setters ---

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public Integer getCuatrimestre() { return cuatrimestre; }
    public void setCuatrimestre(Integer cuatrimestre) { this.cuatrimestre = cuatrimestre; }

    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }
}
