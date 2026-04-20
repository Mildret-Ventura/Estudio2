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
 * AuthController - Controlador de Autenticación
 * 
 * ¿Qué es?: Es un componente de Spring Boot (@RestController) que maneja todas las peticiones HTTP
 * relacionadas con la seguridad, el inicio de sesión y la recuperación de contraseñas.
 * 
 * ¿Para qué sirve?: Sirve como el punto de entrada (API Endpoint) para que el frontend pueda 
 * autenticar usuarios, enviar códigos de recuperación y restablecer contraseñas.
 * 
 * ¿Cómo funciona?: Recibe peticiones JSON, las valida y delega la lógica de negocio a los 
 * servicios correspondientes (AuthService y PasswordRecoveryService). Luego retorna una 
 * respuesta HTTP (ResponseEntity) con el resultado o un error.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Inyección de dependencias: servicios necesarios para la lógica de autenticación
    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;

    public AuthController(AuthService authService, PasswordRecoveryService passwordRecoveryService) {
        this.authService = authService;
        this.passwordRecoveryService = passwordRecoveryService;
    }

    /**
     * Endpoint para iniciar sesión.
     * Recibe: LoginRequest (usuario y contraseña).
     * Retorna: LoginResponse (token JWT e información del usuario).
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Llama al servicio para validar credenciales y generar token
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (DisabledException ex) {
            // Error si la cuenta está desactivada por el admin
            return buildError(HttpStatus.FORBIDDEN, "Cuenta inactiva, contacte al administrador");
        } catch (LockedException ex) {
            // Error si la cuenta está bloqueada temporalmente por intentos fallidos
            return buildError(HttpStatus.FORBIDDEN, "Cuenta bloqueada, intente nuevamente en 15 minutos");
        } catch (BadCredentialsException ex) {
            // Error si el usuario o la contraseña no coinciden
            return buildError(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }
    }

    /**
     * Endpoint para solicitar la recuperación de contraseña.
     * Envía un código al correo electrónico proporcionado.
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
            return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    /**
     * Endpoint para validar el código enviado al correo.
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
     * Endpoint final para cambiar la contraseña después de verificar el código.
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
     * Método auxiliar para construir respuestas de error uniformes.
     */
    private ResponseEntity<Map<String, String>> buildError(HttpStatus status, String mensaje) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A) SOBRE ESTE CÓDIGO:
     * 
     * 1. ¿Qué pasaría si el correo enviado en 'enviar-codigo' no existe en la base de datos?
     *    - Respuesta: El servicio 'passwordRecoveryService' lanzará una excepción (probablemente IllegalArgumentException),
     *      el catch la capturará y devolverá un error 400 Bad Request con el mensaje "Usuario no encontrado".
     * 
     * 2. ¿Qué pasaría si el JSON enviado al login está mal formado?
     *    - Respuesta: Spring Boot lanzará automáticamente un error 400 antes de entrar al método 'login' 
     *      debido a la anotación @Valid y @RequestBody.
     * 
     * 3. ¿Dónde está la lógica si piden cambiar un botón o agregar uno nuevo?
     *    - Respuesta: En este archivo (backend) no hay botones. La lógica de los botones de "Login" o 
     *      "Recuperar Contraseña" está en el frontend, específicamente en [LoginForm.jsx] y [ForgotPasswordForm.jsx].
     *      Si quieres agregar una funcionalidad nueva (como "Login con Google"), tendrías que crear un 
     *      nuevo endpoint @PostMapping aquí y luego crear el botón en el frontend para llamar a ese endpoint.
     * 
     * 4. ¿Qué pasaría si el token generado expira?
     *    - Respuesta: Este controlador no maneja la expiración. La validación del token ocurre en 
     *      [JwtAuthenticationFilter.java] antes de llegar a cualquier controlador. Si expira, el filtro 
     *      rechazará la petición con un error 401 Unauthorized.
     */
}

