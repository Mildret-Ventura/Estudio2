package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.services.AsistenciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AsistenciaController - Controlador de Registro de Asistencias
 * 
 * ¿Qué es?: Es un controlador que maneja el registro de entrada de los alumnos a los eventos, 
 * ya sea de forma automática (QR) o manual (por matrícula).
 * 
 * ¿Para qué sirve?: Permite confirmar que un alumno asistió físicamente al evento, lo cual 
 * es requisito para la posterior emisión de su diploma.
 * 
 * ¿Cómo funciona?: 
 * - Recibe peticiones para registrar asistencia mediante ID de usuario o matrícula.
 * - Verifica que el alumno esté previamente inscrito (PreCheckin).
 * - Controla que no se registre la misma asistencia dos veces.
 * - Permite al administrador consultar la lista de asistentes por evento.
 */
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    /**
     * Registra la asistencia de un alumno (generalmente vía escaneo de QR).
     * Requiere: idUsuario, idEvento y el método de registro.
     */
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

    /**
     * Registra la asistencia de forma manual ingresando la matrícula.
     * Útil cuando el alumno no tiene su código QR a la mano.
     */
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

    /**
     * Obtiene la lista de todos los alumnos que han registrado asistencia en un evento.
     */
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

    /**
     * Permite cambiar el estado de una asistencia (ej. de 'VALIDO' a 'ANULADO').
     */
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

    /**
     * Retorna el número total de asistentes confirmados para un evento.
     */
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

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si un alumno intenta registrar asistencia sin haberse inscrito?
     *    - Respuesta: El servicio lanzará una IllegalArgumentException ("Alumno no inscrito en el evento")
     *      y el controlador devolverá un error 400 Bad Request.
     * 
     * 2. ¿Qué pasaría si se intenta registrar la asistencia después de que el evento terminó?
     *    - Respuesta: La lógica en 'AsistenciaService' debe validar la fecha actual contra la 
     *      fecha del evento. Si ya pasó el tiempo de tolerancia, lanzará un error.
     * 
     * 3. ¿Dónde está la lógica del botón "Ingreso Manual"?
     *    - Respuesta: El botón está en el componente [IngresoManualModal.jsx] del frontend. 
     *      Ese modal pide la matrícula y llama al endpoint '/api/asistencias/manual' definido aquí.
     * 
     * 4. ¿Qué significa el estado 'CONFLICT' (409) en la respuesta?
     *    - Respuesta: Se usa cuando el alumno ya tiene una asistencia registrada para ese 
     *      mismo evento, evitando duplicados.
     */
}
