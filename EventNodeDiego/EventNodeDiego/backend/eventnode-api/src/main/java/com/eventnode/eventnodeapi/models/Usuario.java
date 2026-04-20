package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Cuenta de acceso al sistema. El correo es el identificador de login y el “username” del JWT.
 * <ul>
 *   <li>{@code password}: hash BCrypt en producción; puede existir legado en texto plano migrado al login.</li>
 *   <li>{@code recover_password}: código de recuperación temporal (no es hash).</li>
 *   <li>{@code intentos_fallidos} / {@code bloqueado_hasta}: anti fuerza bruta en {@link com.eventnode.eventnodeapi.services.AuthService}.</li>
 * </ul>
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** Código de 6 dígitos para flujo “olvidé mi contraseña”; se anula al completar el cambio. */
    @Column(name = "recover_password", length = 20)
    private String recoverPassword;

    /** ACTIVO permite login; INACTIVO bloquea aunque el JWT antiguo aún exista en clientes. */
    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('ACTIVO','INACTIVO')")
    private String estado;

    /** Se incrementa en cada fallo de contraseña y se resetea a 0 tras login exitoso. */
    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos;

    /** Si la hora actual es antes de este timestamp, {@link com.eventnode.eventnodeapi.security.CustomUserDetailsService} marca cuenta bloqueada. */
    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    /** EAGER: cada consulta de usuario trae rol para armar {@code ROLE_*} sin query extra explícita. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    public Usuario() {
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRecoverPassword() {
        return recoverPassword;
    }

    public void setRecoverPassword(String recoverPassword) {
        this.recoverPassword = recoverPassword;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

