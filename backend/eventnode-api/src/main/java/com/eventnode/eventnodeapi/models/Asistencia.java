package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Asistencia - Registro de Entrada a Eventos
 * 
 * ¿Qué es?: Es la entidad que representa la confirmación de que un alumno estuvo 
 * presente en un evento determinado.
 * 
 * ¿Para qué sirve?: Sirve como prueba de participación. Sin este registro, el 
 * sistema no permitirá que el alumno reciba su diploma del evento.
 * 
 * ¿Cómo funciona?: 
 * - Guarda el ID del usuario y del evento (llaves foráneas).
 * - Registra la fecha y hora exacta del check-in.
 * - Indica si fue por escaneo de QR o ingreso manual.
 * - Tiene un estado ('PENDIENTE' o 'ASISTIDO') que el admin puede validar.
 */
@Entity
@Table(name = "asistencias")
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Integer idAsistencia;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_evento", nullable = false)
    private Integer idEvento;

    @Column(name = "fecha_checkin")
    private LocalDateTime fechaCheckin;

    @Column(name = "metodo", nullable = false, columnDefinition = "ENUM('QR','MANUAL')")
    private String metodo;

    @Column(name = "estado", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDIENTE'")
    private String estado = "PENDIENTE";

    public Asistencia() {
    }

    // Getters y Setters...

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Por qué el estado inicial es 'PENDIENTE'?
     *    - Respuesta: Para permitir que el administrador verifique que la persona 
     *      realmente entró al recinto antes de confirmarla como "Asistido".
     * 
     * 2. ¿Qué pasa si un alumno borra su cuenta?
     *    - Respuesta: Las asistencias están ligadas al 'id_usuario'. Si se borra el 
     *      usuario, estos registros podrían quedar huérfanos o borrarse en cascada.
     * 
     * 3. ¿Dónde se define si es QR o MANUAL?
     *    - Respuesta: En la columna 'metodo', que es un ENUM. Esto ayuda a saber 
     *      qué tan confiable fue el registro.
     * 
     * 4. ¿Puedo tener dos asistencias para el mismo evento?
     *    - Respuesta: No. La lógica en [AsistenciaService.java] impide crear un 
     *      segundo registro si ya existe uno para esa combinación de usuario/evento.
     */
}
