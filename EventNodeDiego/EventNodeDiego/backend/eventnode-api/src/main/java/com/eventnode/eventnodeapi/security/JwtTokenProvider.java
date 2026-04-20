package com.eventnode.eventnodeapi.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidades JWT basadas en la librería JJWT: firma simétrica HMAC-SHA con una clave secreta configurable.
 * <p>El token guarda como {@code subject} el <strong>correo electrónico</strong> del usuario, que debe
 * coincidir con lo que {@link CustomUserDetailsService} usa como {@code username}.</p>
 */
@Component
public class JwtTokenProvider {

    /**
     * Clave mínima recomendada para HS256: suficiente longitud en bytes. En producción definir
     * {@code app.jwt-secret} en variables de entorno, no dejar el valor por defecto.
     */
    @Value("${app.jwt-secret:SecretKeyToGenerateJWTsAtLeast32CharactersLong}")
    private String jwtSecret;

    /** Duración del token en milisegundos desde su emisión (por defecto ~7 días). */
    @Value("${app.jwt-expiration-milliseconds:604800000}") // 7 days
    private long jwtExpirationDate;

    /**
     * Construye un JWT firmado con expiración.
     *
     * @param authentication objeto creado en {@link com.eventnode.eventnodeapi.services.AuthService#login};
     *                         {@link Authentication#getName()} debe ser el correo del usuario
     * @return cadena compacta del JWT para enviar al cliente en el cuerpo JSON de login
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key())
                .compact();
    }

    /** Deriva la clave HMAC a partir del secreto UTF-8 configurado. */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Obtiene el identificador principal (correo) embebido en el token.
     *
     * @param token JWT completo recibido del cliente
     * @return valor del claim {@code sub} (correo)
     * @throws JwtException si firma o formato son inválidos (el llamador suele haber validado antes)
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
     * Comprueba que el token esté bien formado, firmado con nuestra clave y no caducado.
     *
     * @param token JWT o {@code null}/vacío (en la práctica el filtro ya filtra con {@link org.springframework.util.StringUtils})
     * @return {@code true} si es aceptable; {@code false} ante cualquier error de parsing o firma
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Firma incorrecta, token expirado, malformado, etc. — no propagamos: tratamos como "no autenticado"
            return false;
        }
    }
}
