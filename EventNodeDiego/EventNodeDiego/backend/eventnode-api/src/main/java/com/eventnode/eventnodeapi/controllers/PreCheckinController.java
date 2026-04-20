package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.services.PreCheckinService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API de pre-check-in. {@code IllegalArgumentException} → 400 (datos inválidos); {@code IllegalStateException}
 * → 409 Conflict (reglas de negocio: lleno, duplicado, fuera de plazo).
 */
@RestController
@RequestMapping("/api/precheckin")
public class PreCheckinController {

    private final PreCheckinService preCheckinService;

    public PreCheckinController(PreCheckinService preCheckinService) {
        this.preCheckinService = preCheckinService;
    }

    /** Cuerpo {@code { "idUsuario", "idEvento" }}; requiere rol ALUMNO en seguridad. */
    @PostMapping("/inscribirse")
    public ResponseEntity<?> inscribirse(@RequestBody Map<String, Integer> body) {
        try {
            Integer idUsuario = body.get("idUsuario");
            Integer idEvento = body.get("idEvento");

            if (idUsuario == null || idEvento == null) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "idUsuario e idEvento son requeridos");
                return ResponseEntity.badRequest().body(error);
            }

            preCheckinService.inscribirse(idUsuario, idEvento);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Inscripción exitosa");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IllegalStateException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /** Cancelación sujeta a ventana definida por horas antes de {@code fechaInicio} del evento. */
    @PostMapping("/cancelar")
    public ResponseEntity<?> cancelarInscripcion(@RequestBody Map<String, Integer> body) {
        try {
            Integer idUsuario = body.get("idUsuario");
            Integer idEvento = body.get("idEvento");

            if (idUsuario == null || idEvento == null) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "idUsuario e idEvento son requeridos");
                return ResponseEntity.badRequest().body(error);
            }

            preCheckinService.cancelarInscripcion(idUsuario, idEvento);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Inscripción cancelada exitosamente");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IllegalStateException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /** Staff: inscritos ACTIVO con nombre, correo y matrícula si aplica. */
    @GetMapping("/evento/{idEvento}")
    public ResponseEntity<?> listarInscritos(@PathVariable Integer idEvento) {
        try {
            List<Map<String, Object>> inscritos = preCheckinService.listarInscritos(idEvento);
            return ResponseEntity.ok(inscritos);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /** Alumno: todos sus registros de inscripción con datos del evento embebidos. */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarEventosInscritos(@PathVariable Integer idUsuario) {
        try {
            List<Map<String, Object>> eventos = preCheckinService.listarEventosInscritos(idUsuario);
            return ResponseEntity.ok(eventos);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /** JSON {@code { idEvento, totalInscritos }} para widgets de cupo. */
    @GetMapping("/evento/{idEvento}/count")
    public ResponseEntity<?> contarInscritos(@PathVariable Integer idEvento) {
        try {
            long count = preCheckinService.contarInscritos(idEvento);
            Map<String, Object> response = new HashMap<>();
            response.put("idEvento", idEvento);
            response.put("totalInscritos", count);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
