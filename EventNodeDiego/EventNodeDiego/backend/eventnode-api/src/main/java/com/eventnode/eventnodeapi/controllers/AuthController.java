package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.dtos.LoginRequest;
import com.eventnode.eventnodeapi.dtos.LoginResponse;
import com.eventnode.eventnodeapi.services.AuthService;
import com.eventnode.eventnodeapi.services.PasswordRecoveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Capa HTTP para autenticación y recuperación de contraseña (sin sesión de servidor).
 * <p>Las excepciones de Spring Security en login se mapean a códigos HTTP explícitos para que el
 * cliente móvil/web pueda mostrar mensajes sin inspeccionar el cuerpo de error genérico del framework.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;

    public AuthController(AuthService authService, PasswordRecoveryService passwordRecoveryService) {
        this.authService = authService;
        this.passwordRecoveryService = passwordRecoveryService;
    }

    /**
     * Autenticación con correo y contraseña. No usa {@code AuthenticationManager} del formulario;
     * delega en {@link AuthService#login}.
     *
     * @param request cuerpo JSON validado ({@link LoginRequest}: correo obligatorio formato email, password no vacía)
     * @return 200 con {@link LoginResponse} (incluye JWT); 401 credenciales incorrectas; 403 cuenta inactiva o bloqueada
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (DisabledException ex) {
            // Usuario.estado != ACTIVO — política de negocio
            return buildError(HttpStatus.FORBIDDEN, "Cuenta inactiva, contacte al administrador");
        } catch (LockedException ex) {
            // bloqueado_hasta > ahora tras demasiados intentos fallidos
            return buildError(HttpStatus.FORBIDDEN, "Cuenta bloqueada, intente nuevamente en 15 minutos");
        } catch (BadCredentialsException ex) {
            // Usuario inexistente o password incorrecta (mensaje genérico por seguridad)
            return buildError(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }
    }

    /**
     * Paso 1 del flujo de recuperación: genera código de 6 dígitos, lo guarda en {@code usuarios.recover_password}
     * e intenta enviar correo HTML.
     *
     * @param request mapa JSON con clave {@code "correo"}
     * @return 200 con mensaje informativo; 400 si falta correo o el servicio lanza validación de negocio
     */
    @PostMapping("/recuperar/enviar-codigo")
    public ResponseEntity<?> enviarCodigo(@RequestBody Map<String, String> request) {
        try {
            String correo = request.get("correo");
            if (correo == null || correo.isBlank()) {
                return buildError(HttpStatus.BAD_REQUEST, "El correo es obligatorio");
            }
            passwordRecoveryService.enviarCodigo(correo.trim());
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", "Código enviado al correo");
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            // IllegalArgument: correo no registrado; IllegalState: cuenta inactiva, etc.
            return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    /**
     * Paso 2: comprueba que el código en BD coincida con el enviado por el usuario (sin borrarlo aún).
     *
     * @param request claves {@code "correo"} y {@code "codigo"}
     */
    @PostMapping("/recuperar/verificar-codigo")
    public ResponseEntity<?> verificarCodigo(@RequestBody Map<String, String> request) {
        try {
            String correo = request.get("correo");
            String codigo = request.get("codigo");
            if (correo == null || codigo == null) {
                return buildError(HttpStatus.BAD_REQUEST, "Correo y código son obligatorios");
            }
            passwordRecoveryService.verificarCodigo(correo.trim(), codigo.trim());
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", "Código verificado correctamente");
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    /**
     * Paso 3: valida de nuevo código + política de nueva contraseña, aplica BCrypt y limpia el código de recuperación.
     *
     * @param request claves {@code "correo"}, {@code "codigo"}, {@code "nuevaPassword"}
     */
    @PostMapping("/recuperar/restablecer")
    public ResponseEntity<?> restablecerPassword(@RequestBody Map<String, String> request) {
        try {
            String correo = request.get("correo");
            String codigo = request.get("codigo");
            String nuevaPassword = request.get("nuevaPassword");
            if (correo == null || codigo == null || nuevaPassword == null) {
                return buildError(HttpStatus.BAD_REQUEST, "Todos los campos son obligatorios");
            }
            passwordRecoveryService.restablecerPassword(correo.trim(), codigo.trim(), nuevaPassword);
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", "Contraseña restablecida exitosamente");
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    /**
     * Formato homogéneo de error para el front: siempre {@code {"mensaje":"..."}}.
     *
     * @param status código HTTP deseado
     * @param mensaje texto ya traducido / listo para mostrar al usuario
     */
    private ResponseEntity<Map<String, String>> buildError(HttpStatus status, String mensaje) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
