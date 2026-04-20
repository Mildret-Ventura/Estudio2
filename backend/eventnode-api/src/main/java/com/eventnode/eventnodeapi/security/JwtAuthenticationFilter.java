package com.eventnode.eventnodeapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Filtro de Autenticación JWT
 * 
 * ¿Qué es?: Es un filtro de seguridad que se ejecuta en CADA petición HTTP 
 * que llega al servidor (OncePerRequestFilter).
 * 
 * ¿Para qué sirve?: Su función es interceptar las peticiones, buscar si traen 
 * un token JWT en la cabecera, validarlo y "loguear" al usuario en el contexto 
 * de seguridad de Spring si el token es correcto.
 * 
 * ¿Cómo funciona?: 
 * 1. Extrae el token de la cabecera 'Authorization' (debe empezar con 'Bearer ').
 * 2. Valida el token con el 'JwtTokenProvider'.
 * 3. Si es válido, busca al usuario en la base de datos (UserDetailsService).
 * 4. Crea una "identidad oficial" (Authentication) para esa petición.
 * 5. Permite que la petición continúe hacia el controlador.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Lógica interna del filtro. Se ejecuta antes de llegar a cualquier Endpoint.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtener el token del request
        String token = getTokenFromRequest(request);

        // 2. Validar token y cargar identidad
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // Extraer correo del token
            String username = jwtTokenProvider.getUsername(token);
            // Cargar datos completos del usuario (roles, estado, etc.)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Crear el objeto de autenticación de Spring Security
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // "Loguear" al usuario para ESTA petición específica
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 3. Continuar con el siguiente filtro o con el controlador
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token del header 'Authorization: Bearer <token>'.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Quitar la palabra 'Bearer '
        }
        return null;
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si la petición no trae el header 'Authorization'?
     *    - Respuesta: El filtro simplemente no hará nada ('token' será null) y 
     *      pasará la petición al siguiente filtro. Si el endpoint requiere 
     *      estar logueado, Spring Security lanzará un error 401 más adelante.
     * 
     * 2. ¿Por qué se ejecuta en cada petición?
     *    - Respuesta: Porque las APIs REST son "stateless" (sin estado). El 
     *      servidor no recuerda quién eres, así que debes demostrarlo en cada 
     *      llamada enviando tu token.
     * 
     * 3. ¿Dónde está la lógica de los botones?
     *    - Respuesta: No hay botones involucrados. Esto ocurre "detrás de escena" 
     *      cuando el frontend (React) hace cualquier 'fetch' usando el [apiHelper.js].
     * 
     * 4. ¿Qué pasa si el usuario está inactivo en la base de datos?
     *    - Respuesta: 'userDetailsService.loadUserByUsername' cargará al usuario y 
     *      Spring Security verificará si está habilitado. Si no, rechazará la petición.
     */
}
