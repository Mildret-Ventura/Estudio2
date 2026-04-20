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
 * UsuarioController - Controlador de Perfiles y Gestión de Usuarios
 * 
 * ¿Qué es?: Es el controlador encargado de las operaciones administrativas sobre los 
 * usuarios, así como de la gestión de perfiles individuales.
 * 
 * ¿Para qué sirve?: Permite listar todos los usuarios, registrar nuevos administradores, 
 * actualizar información de perfil y activar/desactivar cuentas.
 * 
 * ¿Cómo funciona?: 
 * - Expone endpoints para obtener perfiles (PerfilResponse).
 * - Permite al SuperAdmin crear otros administradores.
 * - Facilita el cambio de estado (ACTIVO/INACTIVO) de cualquier usuario.
 * - Utiliza 'UsuarioService' para la lógica de negocio y validación de permisos.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Lista todos los usuarios registrados en el sistema.
     * Solo para uso administrativo.
     */
    @GetMapping
    public ResponseEntity<List<PerfilResponse>> listarUsuarios() {
        List<PerfilResponse> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene la información detallada del perfil de un usuario por su ID.
     */
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
     * Registra un nuevo administrador en el sistema.
     * Solo puede ser ejecutado por un SuperAdmin.
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
     * Actualiza los datos del perfil de un usuario.
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

    /**
     * Cambia el estado del usuario (Alterna entre ACTIVO e INACTIVO).
     */
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

    /**
     * Manejador de errores de validación para los campos del registro.
     */
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

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si un Alumno intenta crear un Administrador?
     *    - Respuesta: El 'UsuarioService' verificará el rol del que hace la petición. 
     *      Si no es SuperAdmin, lanzará una SecurityException y devolverá un 403 Forbidden.
     * 
     * 2. ¿Qué pasa si desactivo a un usuario que está logueado?
     *    - Respuesta: La próxima vez que intente realizar una acción que requiera 
     *      el token, el sistema verificará su estado en la base de datos y le 
     *      denegará el acceso.
     * 
     * 3. ¿Dónde está la lógica del botón "Editar Perfil"?
     *    - Respuesta: Está en la página [StudentProfile.jsx] o [AdminPerfil.jsx]. 
     *      Esos componentes envían los nuevos datos a '/api/usuarios/{id}/perfil'.
     * 
     * 4. ¿Puedo cambiar mi contraseña mediante 'actualizarPerfil'?
     *    - Respuesta: No por seguridad. El cambio de contraseña tiene su propio 
     *      flujo controlado mediante el [AuthController.java].
     */
}
