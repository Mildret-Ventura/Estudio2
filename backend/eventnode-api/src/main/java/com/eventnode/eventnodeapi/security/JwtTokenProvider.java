package com.eventnode.eventnodeapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtTokenProvider - Proveedor de Tokens JWT
 * 
 * ¿Qué es?: Es un componente que se encarga de la creación, lectura y validación 
 * de los tokens de seguridad (JWT - JSON Web Token).
 * 
 * ¿Para qué sirve?: Permite que el servidor genere una "llave digital" (token) 
 * cuando un usuario inicia sesión. El frontend guarda esta llave y la envía en 
 * cada petición para demostrar que el usuario está autenticado.
 * 
 * ¿Cómo funciona?: 
 * - Utiliza una "Llave Secreta" (jwtSecret) para firmar digitalmente los tokens.
 * - Establece un tiempo de expiración (7 días por defecto).
 * - Extrae el correo del usuario (subject) desde el token cuando el frontend lo envía.
 */
@Component
public class JwtTokenProvider {

    // Llave secreta cargada desde application.properties (o un valor por defecto)
    @Value("${app.jwt-secret:SecretKeyToGenerateJWTsAtLeast32CharactersLong}")
    private String jwtSecret;

    // Tiempo de vida del token (604,800,000 ms = 7 días)
    @Value("${app.jwt-expiration-milliseconds:604800000}") 
    private long jwtExpirationDate;

    /**
     * Crea un nuevo token JWT basado en la identidad del usuario logueado.
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .subject(username) // El "sujeto" del token es el correo del usuario
                .issuedAt(new Date()) // Fecha de creación
                .expiration(expireDate) // Fecha de vencimiento
                .signWith(key()) // Firma con la llave secreta
                .compact();
    }

    /**
     * Convierte el string secreto en una llave criptográfica real.
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Lee un token y extrae el nombre de usuario (correo) guardado en él.
     */
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Verifica que el token sea auténtico, no haya sido alterado y no haya expirado.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Si el token es inválido o expiró, retorna false
            return false;
        }
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si alguien intenta usar un token de hace un mes?
     *    - Respuesta: El método 'validateToken' detectará que la fecha de expiración 
     *      ya pasó y retornará 'false', denegando el acceso.
     * 
     * 2. ¿Qué pasa si cambio la 'jwtSecret' en el servidor?
     *    - Respuesta: Todos los tokens emitidos anteriormente dejarán de ser válidos 
     *      y todos los usuarios deberán iniciar sesión de nuevo.
     * 
     * 3. ¿Dónde está la lógica de los botones?
     *    - Respuesta: Este componente se usa en el [AuthService.java] cuando el 
     *      usuario pulsa el botón de "Login" en el frontend.
     * 
     * 4. ¿Puedo ver qué hay dentro de un token?
     *    - Respuesta: Sí, cualquier persona puede ver el contenido (subject, fechas) 
     *      usando herramientas como jwt.io, pero nadie puede ALTERARLO sin 
     *      romper la firma digital.
     */
}
