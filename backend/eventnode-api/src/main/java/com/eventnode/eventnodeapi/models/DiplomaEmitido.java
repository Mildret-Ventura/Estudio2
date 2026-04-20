package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * DiplomaEmitido - Historial de Entrega de Reconocimientos
 * 
 * ¿Qué es?: Es la entidad que registra cada vez que un diploma es generado y 
 * enviado a un alumno.
 * 
 * ¿Para qué sirve?: Funciona como un log de auditoría. Permite saber a quién 
 * ya se le envió su diploma y quién tuvo errores en el proceso de envío por correo.
 * 
 * ¿Cómo funciona?: 
 * - Vincula un diseño de diploma ('id_diploma') con un alumno ('id_usuario').
 * - Guarda la fecha exacta del envío.
 * - Registra si el envío fue exitoso ('ENVIADO') o falló ('ERROR').
 */
@Entity
@Table(name = "diplomas_emitidos")
public class DiplomaEmitido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emitido")
    private Integer idEmitido;

    @Column(name = "id_diploma", nullable = false)
    private Integer idDiploma;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "estado_envio", nullable = false, columnDefinition = "ENUM('ENVIADO','ERROR')")
    private String estadoEnvio;

    public DiplomaEmitido() {
    }

    // Getters y Setters...

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasa si el estado es 'ERROR'?
     *    - Respuesta: El administrador lo verá en rojo en su panel y podrá 
     *      intentar reenviar el diploma una vez corregido el problema (ej. corregir el correo).
     * 
     * 2. ¿Puedo borrar estos registros?
     *    - Respuesta: No se recomienda, ya que perderías el historial de qué 
     *      alumnos ya recibieron su diploma oficial.
     * 
     * 3. ¿Dónde consulto esto?
     *    - Respuesta: En la página [AdminDiplomas.jsx], al ver las estadísticas 
     *      de cada diploma.
     * 
     * 4. ¿Se guarda el archivo PDF aquí?
     *    - Respuesta: No, el PDF se genera en el aire cada vez que se necesita. 
     *      Aquí solo se guarda el registro de que ya fue enviado.
     */
}
