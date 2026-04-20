package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * AlumnoRepository - Repositorio de Estudiantes
 * 
 * ¿Qué es?: Es el componente de persistencia para la tabla 'alumnos'.
 * 
 * ¿Para qué sirve?: Provee métodos para buscar alumnos por sus atributos 
 * académicos específicos, como la matrícula.
 * 
 * ¿Cómo funciona?: 
 * - Se conecta a la tabla 'alumnos' usando JPA.
 * - Permite validar que no se registren dos alumnos con la misma matrícula.
 */
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    /**
     * Busca un alumno usando su matrícula institucional.
     */
    Optional<Alumno> findByMatricula(String matricula);

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasa si busco una matrícula con espacios?
     *    - Respuesta: JPA hará la búsqueda exacta. Es recomendable limpiar 
     *      el string con .trim() en el servicio antes de llamar a este método.
     * 
     * 2. ¿Puedo buscar alumnos por cuatrimestre?
     *    - Respuesta: Sí, agregando 'List<Alumno> findByCuatrimestre(Integer c);'.
     * 
     * 3. ¿Dónde está la lógica de los botones?
     *    - Respuesta: Los botones de la vista de admin (ej. Editar Alumno) 
     *      llaman al servicio, el cual finalmente usa este repositorio para 
     *      guardar o buscar datos.
     */
}

