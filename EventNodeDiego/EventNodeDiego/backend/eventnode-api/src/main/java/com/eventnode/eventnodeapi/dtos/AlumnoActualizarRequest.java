package com.eventnode.eventnodeapi.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Actualización de perfil de alumno (sin cambio de contraseña en este DTO). */
public class AlumnoActualizarRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    private String apellidoMaterno;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingrese una dirección de correo electrónico válida")
    private String correo;

    @NotBlank(message = "El sexo es obligatorio")
    private String sexo;

    @NotNull(message = "El cuatrimestre es obligatorio")
    private Integer cuatrimestre;
    
    @NotNull(message = "La edad es obligatoria")
    private Integer edad;

    public AlumnoActualizarRequest() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this. sexo = sexo; }

    public Integer getCuatrimestre() { return cuatrimestre; }
    public void setCuatrimestre(Integer cuatrimestre) { this.cuatrimestre = cuatrimestre; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
}
