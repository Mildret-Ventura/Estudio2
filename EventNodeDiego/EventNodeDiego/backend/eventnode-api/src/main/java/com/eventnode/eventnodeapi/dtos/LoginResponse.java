package com.eventnode.eventnodeapi.dtos;

/** Respuesta exitosa de login: datos de perfil, rol y JWT. */
public class LoginResponse {

    private String mensaje;
    private String rol;
    private Integer idUsuario;

    // Profile details
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correo;
    private String matricula;
    private String sexo;
    private Integer cuatrimestre;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(String mensaje, String rol, Integer idUsuario, String nombre,
                         String apellidoPaterno, String apellidoMaterno, String correo,
                         String matricula, String sexo, Integer cuatrimestre, String token) {
        this.mensaje = mensaje;
        this.rol = rol;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.correo = correo;
        this.matricula = matricula;
        this.sexo = sexo;
        this.cuatrimestre = cuatrimestre;
        this.token = token;
    }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

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

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public Integer getCuatrimestre() { return cuatrimestre; }
    public void setCuatrimestre(Integer cuatrimestre) { this.cuatrimestre = cuatrimestre; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
