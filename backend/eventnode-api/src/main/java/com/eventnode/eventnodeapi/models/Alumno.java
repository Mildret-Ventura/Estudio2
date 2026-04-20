package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;

/**
 * Alumno - Perfil Específico de Estudiante
 * 
 * ¿Qué es?: Es la entidad que guarda los datos académicos y personales de los 
 * estudiantes registrados en la plataforma.
 * 
 * ¿Para qué sirve?: Almacena información crítica como la matrícula, el cuatrimestre 
 * y la edad, datos necesarios para la emisión de diplomas y el filtrado de eventos.
 * 
 * ¿Cómo funciona?: 
 * - Al igual que 'Administrador', comparte el ID con la tabla 'usuarios'.
 * - El campo 'matricula' es único, impidiendo que dos personas usen la misma.
 * - Implementa 'Persistable' para manejar correctamente el guardado de nuevos alumnos.
 */
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

    // Getters y Setters...

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

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si un alumno cumple años? ¿Se actualiza la 'edad'?
     *    - Respuesta: Actualmente no se actualiza sola. Se calcula al momento 
     *      del registro en el [AlumnoService.java]. Sería mejor calcularla 
     *      dinámicamente cada vez que se consulte para que siempre sea exacta.
     * 
     * 2. ¿Por qué la matrícula es 'unique'?
     *    - Respuesta: Para evitar que un estudiante cree múltiples cuentas 
     *      usando la misma identificación escolar.
     * 
     * 3. ¿Dónde está la lógica si piden agregar el campo "Carrera"?
     *    - Respuesta: Debes agregarlo aquí como una columna: 
     *      '@Column(name = "carrera", length = 100) private String carrera;'.
     * 
     * 4. ¿Qué es '@MapsId'?
     *    - Respuesta: Es una anotación que le dice a JPA que el ID de esta tabla 
     *      es exactamente el mismo ID de la tabla relacionada (Usuario).
     */
}

