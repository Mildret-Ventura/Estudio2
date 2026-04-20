package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

/**
 * Categoria - Clasificación de Eventos
 * 
 * ¿Qué es?: Es una entidad simple que define los tipos o géneros de eventos 
 * (ej. Académico, Deportivo, Social).
 * 
 * ¿Para qué sirve?: Permite organizar los eventos para que los alumnos puedan 
 * filtrarlos según sus intereses en la cartelera principal.
 * 
 * ¿Cómo funciona?: 
 * - Guarda un nombre único para evitar categorías repetidas.
 * - Sirve como una "etiqueta" que se vincula a muchos eventos (1 a N).
 */
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    public Categoria() {
    }

    // Getters y Setters...

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Puedo cambiarle el nombre a una categoría?
     *    - Respuesta: Sí, al cambiar el nombre aquí, todos los eventos asociados 
     *      mostrarán automáticamente el nuevo nombre.
     * 
     * 2. ¿Qué pasa si borro una categoría?
     *    - Respuesta: Si tiene eventos vinculados, la base de datos impedirá el 
     *      borrado por "Integridad Referencial".
     * 
     * 3. ¿Dónde está la lista de categorías en el sistema?
     *    - Respuesta: El administrador las gestiona en [AdminCategorias.jsx] y el 
     *      estudiante las ve como filtros en [StudentHome.jsx].
     * 
     * 4. ¿Por qué el nombre es 'unique'?
     *    - Respuesta: Para evitar confusiones como tener "Taller" y "Taller " (con espacio) 
     *      como dos categorías distintas.
     */
}

