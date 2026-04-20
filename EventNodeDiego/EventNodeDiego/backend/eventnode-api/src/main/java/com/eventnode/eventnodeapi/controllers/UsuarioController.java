package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.dtos.AdminRegistroRequest;
import com.eventnode.eventnodeapi.dtos.PerfilResponse;
import com.eventnode.eventnodeapi.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD parcial de usuarios vistos como {@link PerfilResponse}.
 * <p>Rutas sensibles exigen roles en {@link com.eventnode.eventnodeapi.config.SecurityConfig}; aquí solo
 * se traduce {@link SecurityException} del servicio de alta admin a HTTP 403.</p>
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Listado completo para panel administrativo (puede ser costoso en BD — ver {@link UsuarioService#listarTodos}). */
    @GetMapping
    public ResponseEntity<List<PerfilResponse>> listarUsuarios() {
        List<PerfilResponse> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    /** Perfil enriquecido según rol (alumno vs admin). Requiere JWT; comprobar autorización de id en evolución futura. */
    @GetMapping("/{id}/perfil")
    public ResponseEntity<?> obtenerPerfil(@PathVariable("id") Integer id) {
        try {
            PerfilResponse perfil = usuarioService.obtenerPerfil(id);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    /**
     * Crea usuario rol ADMINISTRADOR + fila administrador. El cuerpo incluye {@code idSolicitante}.
     *
     * @return 200 con perfil creado; 403 si el solicitante no es admin; 400 validación/negocio
     */
    @PostMapping("/admin")
    public ResponseEntity<?> registrarAdmin(@Valid @RequestBody AdminRegistroRequest request) {
        try {
            PerfilResponse perfil = usuarioService.registrarAdmin(request);
            return ResponseEntity.ok(perfil);
        } catch (SecurityException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", ex.getMessage());
            return ResponseEntity.status(403).body(body);
        } catch (IllegalArgumentException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    /**
     * PATCH-like vía PUT: solo claves presentes en el mapa actualizan columnas (nombre, apellidos).
     */
    @PutMapping("/{id}/perfil")
    public ResponseEntity<?> actualizarPerfil(@PathVariable("id") Integer id,
                                               @RequestBody Map<String, Object> datos) {
        try {
            PerfilResponse perfil = usuarioService.actualizarPerfil(id, datos);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    /** Toggle ACTIVO ↔ INACTIVO según estado actual almacenado. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable("id") Integer id) {
        try {
            usuarioService.cambiarEstado(id);
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", "Estado actualizado con éxito");
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getDefaultMessage())
                .orElse("Error de validación");
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", mensaje);
        return ResponseEntity.badRequest().body(body);
    }
}
