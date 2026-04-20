package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

/**
 * Administrador - Perfil Específico de Administrador
 * 
 * ¿Qué es?: Es una entidad JPA que extiende la información de un 'Usuario' 
 * para aquellos que tienen privilegios de gestión en el sistema.
 * 
 * ¿Para qué sirve?: Diferencia entre administradores normales y el Administrador 
 * Principal (SuperAdmin), quien tiene permisos para crear otros administradores.
 * 
 * ¿Cómo funciona?: 
 * - Usa '@MapsId' para compartir la misma Llave Primaria (ID) con la tabla 'usuarios'.
 * - Implementa 'Persistable' para optimizar la forma en que Spring Data JPA 
 *   guarda nuevos registros cuando el ID no es autoincremental en esta tabla.
 */
@Entity
@Table(name = "administradores")
public class Administrador implements Persistable<Integer> {

    @Transient
    private boolean isNew = true; // Control interno para optimización de guardado

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    // Relación 1 a 1: Todo administrador es forzosamente un Usuario
    @OneToOne
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "es_principal")
    private Boolean esPrincipal; // True = SuperAdmin, False = Admin normal

    public Administrador() {
    }

    // Getters y Setters...
    
    @Override
    public Integer getId() {
        return idUsuario;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    /**
     * Marca la entidad como "no nueva" después de cargarla o guardarla.
     */
    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Por qué esta tabla no tiene un ID autoincremental propio?
     *    - Respuesta: Porque utiliza el ID del usuario al que pertenece. Así aseguramos 
     *      que un registro de administrador siempre esté ligado a un usuario único.
     * 
     * 2. ¿Qué privilegios tiene el 'esPrincipal = true'?
     *    - Respuesta: Principalmente la capacidad de crear y gestionar a otros 
     *      administradores en el [UsuarioService.java].
     * 
     * 3. ¿Dónde está la lógica si piden que un admin pueda tener una oficina asignada?
     *    - Respuesta: Deberías agregar un nuevo campo '@Column(name = "oficina")' 
     *      en este archivo.
     * 
     * 4. ¿Puedo borrar un administrador sin borrar al usuario?
     *    - Respuesta: Técnicamente sí, pero el usuario dejaría de tener rol admin. 
     *      El sistema está diseñado para que la relación sea integral.
     */
}

