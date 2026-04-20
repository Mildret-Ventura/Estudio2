package com.eventnode.eventnodeapi.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EventoCreateRequest - Objeto de Transferencia para Crear Eventos
 * 
 * ¿Qué es?: Es un DTO (Data Transfer Object) que define exactamente qué datos 
 * espera el backend desde el frontend para registrar un nuevo evento.
 * 
 * ¿Para qué sirve?: Sirve para desacoplar el modelo de base de datos de la 
 * interfaz de usuario y para realizar validaciones automáticas de entrada.
 * 
 * ¿Cómo funciona?: 
 * - Utiliza anotaciones de validación (@NotBlank, @NotNull, @Positive) para 
 *   asegurar que los datos sean correctos antes de procesarlos.
 * - Spring Boot lee estas anotaciones y devuelve un error 400 automáticamente 
 *   si alguna no se cumple.
 */
public class EventoCreateRequest {

    // Imagen del banner en formato Base64
    private String banner;

    @NotBlank(message = "El nombre del evento es obligatorio")
    private String nombre;

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @NotNull(message = "La capacidad máxima es obligatoria")
    @Positive(message = "La capacidad máxima debe ser mayor a cero")
    private Integer capacidadMaxima;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    @NotNull(message = "El tiempo de cancelación es obligatorio")
    @Positive(message = "El tiempo de cancelación debe ser mayor a cero")
    private Integer tiempoCancelacionHoras;

    // Formato de fecha esperado: 2024-04-19T10:00:00
    @NotNull(message = "La fecha de inicio es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFin;

    @NotNull(message = "El tiempo de tolerancia es obligatorio")
    @PositiveOrZero(message = "El tiempo de tolerancia debe ser mayor o igual a cero")
    private Integer tiempoToleranciaMinutos;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    // Lista de IDs de organizadores seleccionados en el modal
    private List<Integer> organizadores;

    // ID del Administrador que está creando el evento
    @NotNull(message = "Se debe indicar quién crea el evento")
    private Integer idCreador;

    public EventoCreateRequest() {
    }

    // Getters y Setters...

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasa si el frontend envía una capacidad de -5?
     *    - Respuesta: La anotación '@Positive' lanzará un error de validación 
     *      antes de que el código llegue al servicio.
     * 
     * 2. ¿Cómo sabe el backend el formato de la fecha?
     *    - Respuesta: Gracias a '@JsonFormat', Spring sabe cómo convertir el 
     *      string del JSON a un objeto LocalDateTime de Java.
     * 
     * 3. ¿Dónde está la lógica de los botones?
     *    - Respuesta: Este archivo no tiene botones, pero define los campos que 
     *      el botón "Guardar" del [CrearEventoModal.jsx] debe llenar.
     * 
     * 4. ¿Puedo agregar un campo "Precio"?
     *    - Respuesta: Sí, agregando 'private Double precio;' y su getter/setter aquí.
     */
}
