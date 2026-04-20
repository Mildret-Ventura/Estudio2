package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.dtos.AlumnoRegistroRequest;
import com.eventnode.eventnodeapi.services.AlumnoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * AlumnoController - Controlador de Gestión de Alumnos
 * 
 * ¿Qué es?: Es un controlador REST (@RestController) encargado de las operaciones 
 * públicas y privadas relacionadas con los estudiantes (alumnos).
 * 
 * ¿Para qué sirve?: Permite el registro de nuevos alumnos (auto-registro) y la 
 * actualización de sus perfiles.
 * 
 * ¿Cómo funciona?: 
 * - Recibe peticiones JSON mapeadas a DTOs (AlumnoRegistroRequest, AlumnoActualizarRequest).
 * - Valida los datos automáticamente mediante @Valid.
 * - Delega la lógica de persistencia al servicio 'AlumnoService'.
 * - Incluye manejadores de excepciones específicos para dar respuestas claras al frontend.
 */
@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    // Inyección del servicio de alumnos
    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    /**
     * Endpoint para el registro público de nuevos alumnos.
     */
    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(@Valid @RequestBody AlumnoRegistroRequest request) {
        alumnoService.registrarAlumno(request);
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", "Cuenta creada con éxito");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * Endpoint para actualizar la información de un alumno existente.
     */
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> actualizarAlumno(
            @org.springframework.web.bind.annotation.PathVariable Integer id,
            @Valid @RequestBody com.eventnode.eventnodeapi.dtos.AlumnoActualizarRequest request) {
        alumnoService.actualizarAlumno(id, request);
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", "Alumno actualizado con éxito");
        return ResponseEntity.ok(body);
    }

    // --- MANEJADORES DE EXCEPCIONES ESPECÍFICOS ---

    /**
     * Captura errores de validación (campos obligatorios, formatos, etc).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> body = new HashMap<>();
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Error de validación en los datos enviados");
        body.put("mensaje", mensaje);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Captura errores cuando el JSON está mal formado o hay tipos de datos incorrectos.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        Map<String, String> body = new HashMap<>();
        String msg = ex.getMessage();
        if (msg != null && msg.contains("fechaNacimiento")) {
            body.put("mensaje", "Formato de fecha inválido. Use el formato AAAA-MM-DD");
        } else if (msg != null && msg.contains("cuatrimestre")) {
            body.put("mensaje", "El cuatrimestre debe ser un número válido");
        } else {
            body.put("mensaje", "Error en el formato de los datos enviados");
        }
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> manejarEstadoInvalido(IllegalStateException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si el correo o la matrícula ya están registrados?
     *    - Respuesta: El servicio 'alumnoService' lanzará una IllegalStateException 
     *      que será capturada por 'manejarEstadoInvalido', devolviendo un error 400 
     *      con el mensaje "El correo ya está en uso" o similar.
     * 
     * 2. ¿Cómo se valida que la contraseña sea segura en el registro?
     *    - Respuesta: La validación reside en el DTO [AlumnoRegistroRequest.java] usando 
     *      anotaciones como @Size o @Pattern.
     * 
     * 3. ¿Dónde está la lógica si piden agregar un campo "Teléfono" al alumno?
     *    - Respuesta: Debes agregarlo en la entidad [Alumno.java], en los DTOs de registro 
     *      y actualización, y ajustar el 'AlumnoService' para guardarlo.
     * 
     * 4. ¿Este controlador maneja la seguridad de quién puede actualizar?
     *    - Respuesta: No directamente. La seguridad de los métodos se configura en 
     *      [SecurityConfig.java], donde se define que solo el propio alumno o un 
     *      admin pueden acceder a los endpoints de edición.
     */
}
