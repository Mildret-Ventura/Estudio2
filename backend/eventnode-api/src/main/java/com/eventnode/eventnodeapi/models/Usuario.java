package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Usuario - Modelo de Datos para Usuarios
 * 
 * ¿Qué es?: Es una entidad de JPA (@Entity) que representa la tabla 'usuarios' en la base de datos.
 * 
 * ¿Para qué sirve?: Define la estructura de un usuario en el sistema, incluyendo sus datos personales,
 * credenciales, estado de la cuenta y su rol.
 * 
 * ¿Cómo funciona?: Mapea cada campo de la clase a una columna de la base de datos. Se utiliza por 
 * Hibernate para realizar operaciones CRUD (Crear, Leer, Actualizar, Borrar).
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    // Identificador único autoincremental
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    // Datos personales
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    // Credenciales y Seguridad
    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    // Código temporal para recuperación de contraseña
    @Column(name = "recover_password", length = 20)
    private String recoverPassword;

    // Estado y control de acceso
    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('ACTIVO','INACTIVO')")
    private String estado;

    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    // Relación con la tabla de roles
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    public Usuario() {
    }

    // Getters y Setters para acceder y modificar los datos del objeto
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

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A) SOBRE ESTE CÓDIGO:
     * 
     * 1. ¿Qué pasaría si intento registrar un usuario con un correo que ya existe?
     *    - Respuesta: La base de datos lanzará un error de "Unique Constraint Violation" 
     *      debido al atributo 'unique = true' en la columna correo.
     * 
     * 2. ¿Qué es 'recover_password'?
     *    - Respuesta: Es un campo temporal donde se guarda el código numérico de 6 dígitos 
     *      cuando un usuario solicita recuperar su contraseña.
     * 
     * 3. ¿Dónde está la lógica si piden agregar un botón de "Desactivar Usuario"?
     *    - Respuesta: El botón estaría en [AdminEstudiantes.jsx] o [EditarEstudianteModal.jsx]. 
     *      Ese botón llamaría a una API que cambie el campo 'estado' de este modelo a 'INACTIVO'.
     * 
     * 4. ¿Por qué el rol se carga con FetchType.EAGER?
     *    - Respuesta: Para asegurar que siempre que obtengamos un usuario, sepamos qué rol 
     *      tiene sin necesidad de hacer una segunda consulta a la base de datos.
     */
}

