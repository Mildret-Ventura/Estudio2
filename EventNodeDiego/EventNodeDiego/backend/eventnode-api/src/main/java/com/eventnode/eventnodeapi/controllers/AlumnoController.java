package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.dtos.AlumnoRegistroRequest;
import com.eventnode.eventnodeapi.services.AlumnoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * API de alumnos: alta pública y actualización de perfil académico.
 * <p>Los {@link ExceptionHandler} de esta clase convierten fallos de validación y de negocio en
 * respuestas JSON homogéneas {@code {"mensaje":"..."}} con HTTP 400, en lugar de stacktrace HTML por defecto.</p>
 */
@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    /**
     * Registro sin autenticación previa (ruta {@code permitAll} en seguridad).
     *
     * @return 201 Created con mensaje de confirmación
     */
    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(@Valid @RequestBody AlumnoRegistroRequest request) {
        alumnoService.registrarAlumno(request);
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", "Cuenta creada con éxito");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * Actualiza nombres, correo, sexo, cuatrimestre y edad. La seguridad de “solo el dueño” debe
     * alinearse con reglas globales (JWT + comprobación de id si se expone a alumnos).
     *
     * @param id PK compartida usuario/alumno
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> actualizarAlumno(
            @PathVariable Integer id,
            @Valid @RequestBody com.eventnode.eventnodeapi.dtos.AlumnoActualizarRequest request) {
        alumnoService.actualizarAlumno(id, request);
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", "Alumno actualizado con éxito");
        return ResponseEntity.ok(body);
    }

    /** Primera violación de {@code @NotBlank}, {@code @Email}, etc. en el cuerpo JSON. */
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
     * Jackson no puede mapear tipos (p. ej. fecha mal formada): se devuelve mensaje amigable
     * inspeccionando el texto interno del error (frágil pero práctico para el cliente actual).
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
}
