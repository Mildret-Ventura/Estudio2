package com.eventnode.eventnodeapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Diploma - Diseño y Configuración de Reconocimientos
 * 
 * ¿Qué es?: Es la entidad que guarda las plantillas y firmas necesarias para 
 * generar diplomas en un evento específico.
 * 
 * ¿Para qué sirve?: Almacena los archivos base (JRXML y firmas) para que el 
 * sistema pueda generar miles de PDFs personalizados automáticamente.
 * 
 * ¿Cómo funciona?: 
 * - Guarda el 'plantilla_pdf' (en realidad un archivo JRXML de JasperReports) 
 *   y la 'firma_imagen' en formato de texto largo (Base64).
 * - Está vinculado 1 a 1 con un Evento (id_evento es único aquí).
 */
@Entity
@Table(name = "diplomas")
public class Diploma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diploma")
    private Integer idDiploma;

    @Column(name = "id_evento", nullable = false, unique = true)
    private Integer idEvento;

    @Column(name = "nombre_evento", nullable = false, length = 200)
    private String nombreEvento;

    @Column(name = "firma", nullable = false, length = 255)
    private String firma;

    @Column(name = "diseno", nullable = false, length = 255)
    private String diseno;

    @Lob
    @Column(name = "plantilla_pdf", columnDefinition = "LONGTEXT")
    private String plantillaPdf;

    @Lob
    @Column(name = "firma_imagen", columnDefinition = "LONGTEXT")
    private String firmaImagen;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('ACTIVO','ELIMINADO')")
    private String estado;

    public Diploma() {
    }

    // Getters y Setters...

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Por qué se usa 'LONGTEXT' para la plantilla?
     *    - Respuesta: Porque los archivos JRXML o las imágenes en Base64 pueden ser 
     *      muy pesados, y un campo de texto normal no tendría capacidad suficiente.
     * 
     * 2. ¿Se puede tener más de un diseño de diploma por evento?
     *    - Respuesta: No, el sistema limita a un solo diseño por evento para 
     *      mantener la consistencia oficial.
     * 
     * 3. ¿Dónde sube el administrador estos archivos?
     *    - Respuesta: En la página [AdminDiplomas.jsx] del frontend, a través 
     *      del modal de creación/edición.
     * 
     * 4. ¿Qué pasa si cambio la firma después de haber emitido diplomas?
     *    - Respuesta: Los diplomas nuevos que se emitan tendrán la firma nueva, 
     *      pero los que ya se enviaron por correo seguirán teniendo la firma anterior.
     */
}
