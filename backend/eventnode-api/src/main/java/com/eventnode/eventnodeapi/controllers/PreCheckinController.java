package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.services.PreCheckinService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PreCheckinController - Controlador de Inscripciones (Pre-Checkin)
 * 
 * ¿Qué es?: Es el controlador que gestiona la reserva de lugar o inscripción 
 * previa de los estudiantes a los eventos.
 * 
 * ¿Para qué sirve?: Permite a los alumnos asegurar su lugar en un evento y a los 
 * administradores conocer la demanda antes de que el evento inicie.
 * 
 * ¿Cómo funciona?: 
 * - Permite inscribirse ('/inscribirse') verificando disponibilidad de cupo.
 * - Permite cancelar la inscripción ('/cancelar') respetando los tiempos límite.
 * - Provee listas de inscritos por evento y eventos inscritos por usuario.
 */
@RestController
@RequestMapping("/api/precheckin")
public class PreCheckinController {

    private final PreCheckinService preCheckinService;

    public PreCheckinController(PreCheckinService preCheckinService) {
        this.preCheckinService = preCheckinService;
    }

    /**
     * Inscribe a un usuario en un evento.
     * Valida que haya cupo y que el evento no haya pasado.
     */
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

    /**
     * Cancela la inscripción de un usuario a un evento.
     * Solo es posible si se hace con la antelación configurada en el evento.
     */
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

    /**
     * Lista todos los alumnos inscritos en un evento específico.
     */
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

    /**
     * Lista todos los eventos a los que un estudiante se ha inscrito.
     */
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

    /**
     * Obtiene el conteo actual de personas inscritas en un evento.
     */
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

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasa si el cupo del evento está lleno?
     *    - Respuesta: El servicio lanzará una IllegalStateException ("El evento ya no tiene cupo")
     *      y el controlador devolverá un error 409 Conflict.
     * 
     * 2. ¿Puedo cancelar mi inscripción 10 minutos antes del evento?
     *    - Respuesta: No, si el evento tiene configurado un 'tiempoCancelacionHoras' mayor a 0.
     *      El sistema bloqueará la cancelación y lanzará un error.
     * 
     * 3. ¿Dónde está el botón de "Inscribirse"?
     *    - Respuesta: En la página [StudentEventDetail.jsx]. Ese botón llama a 
     *      '/api/precheckin/inscribirse' enviando el ID del usuario y del evento.
     * 
     * 4. ¿Qué pasa si un alumno ya está inscrito e intenta inscribirse de nuevo?
     *    - Respuesta: El sistema detectará el registro duplicado y devolverá un error.
     */
}
