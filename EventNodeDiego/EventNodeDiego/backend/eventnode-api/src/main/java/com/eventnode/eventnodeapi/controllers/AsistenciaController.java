package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.services.AsistenciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Check-in y administración de filas {@code asistencias}. Los JSON de entrada usan {@code Map}
 * porque el cliente envía números que a veces llegan como {@link Double} en JSON — cast directo a
 * {@link Integer} puede fallar si el front no envía entero estricto.
 */
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    /** Campos requeridos: {@code idUsuario}, {@code idEvento}, {@code metodo} (ej. QR). */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarAsistencia(@RequestBody Map<String, Object> body) {
        try {
            Integer idUsuario = (Integer) body.get("idUsuario");
            Integer idEvento = (Integer) body.get("idEvento");
            String metodo = (String) body.get("metodo");

            if (idUsuario == null || idEvento == null || metodo == null) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "idUsuario, idEvento y metodo son requeridos");
                return ResponseEntity.badRequest().body(error);
            }

            asistenciaService.registrarAsistencia(idUsuario, idEvento, metodo);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Asistencia registrada exitosamente");
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

    /** {@code matricula} + {@code idEvento}; {@code metodo} por defecto MANUAL si no es String. */
    @PostMapping("/manual")
    public ResponseEntity<?> registrarAsistenciaManual(@RequestBody Map<String, Object> body) {
        try {
            String matricula = (String) body.get("matricula");
            Integer idEvento = (Integer) body.get("idEvento");
            String metodo = body.get("metodo") instanceof String m ? m : "MANUAL";

            if (matricula == null || idEvento == null) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "matricula e idEvento son requeridos");
                return ResponseEntity.badRequest().body(error);
            }

            asistenciaService.registrarAsistenciaManual(matricula, idEvento, metodo);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Asistencia registrada exitosamente");
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

    /** Lista asistencias del evento con datos de usuario resueltos. */
    @GetMapping("/evento/{idEvento}")
    public ResponseEntity<?> listarAsistencias(@PathVariable Integer idEvento) {
        try {
            List<Map<String, Object>> asistencias = asistenciaService.listarAsistencias(idEvento);
            return ResponseEntity.ok(asistencias);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /** Body {@code { "estado": "ASISTIDO" | "PENDIENTE" }} — solo roles admin en configuración de seguridad. */
    @PatchMapping("/{idAsistencia}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer idAsistencia, @RequestBody Map<String, Object> body) {
        try {
            String estado = (String) body.get("estado");
            if (estado == null) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "El campo estado es requerido");
                return ResponseEntity.badRequest().body(error);
            }

            asistenciaService.actualizarEstado(idAsistencia, estado);

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Estado actualizado exitosamente");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /** Totales de filas de asistencia por evento (todas, sin filtrar por estado). */
    @GetMapping("/evento/{idEvento}/count")
    public ResponseEntity<?> contarAsistencias(@PathVariable Integer idEvento) {
        try {
            long count = asistenciaService.contarAsistencias(idEvento);
            Map<String, Object> response = new HashMap<>();
            response.put("idEvento", idEvento);
            response.put("totalAsistencias", count);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error interno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
