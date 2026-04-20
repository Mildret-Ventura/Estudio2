package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

/**
 * Organizador - Responsable de un Evento
 * 
 * ¿Qué es?: Es la entidad que representa a las personas o departamentos que 
 * organizan y ejecutan un evento.
 * 
 * ¿Para qué sirve?: Permite dar crédito y contacto a los responsables. Los 
 * estudiantes pueden ver quién organiza cada evento en los detalles.
 * 
 * ¿Cómo funciona?: 
 * - Guarda el nombre, correo y una breve descripción del organizador.
 * - Un evento puede tener múltiples organizadores asociados mediante una 
 *   tabla intermedia (Muchos a Muchos).
 */
@Entity
@Table(name = "organizadores")
public class Organizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_organizador")
    private Integer idOrganizador;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "correo", length = 150)
    private String correo;

    public Organizador() {
    }

    // Getters y Setters...

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasa si cambio el correo de un organizador?
     *    - Respuesta: Se actualizará en todos los eventos donde aparezca, ya 
     *      que los eventos solo guardan una referencia a su ID.
     * 
     * 2. ¿Puedo borrar a un organizador?
     *    - Respuesta: Sí, pero primero deberías quitarlo de los eventos donde 
     *      esté asignado para evitar errores de llaves foráneas.
     * 
     * 3. ¿Dónde gestiono la lista de organizadores?
     *    - Respuesta: En la página [AdminOrganizadores.jsx].
     * 
     * 4. ¿Un organizador debe ser un usuario del sistema?
     *    - Respuesta: No necesariamente. Pueden ser entes externos o departamentos 
     *      que no requieren loguearse a la plataforma.
     */
}

