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
 * Filtro servlet que se ejecuta <strong>una vez por petición HTTP</strong> (hereda {@link OncePerRequestFilter}).
 * <p>Orden en la cadena: se registra <em>antes</em> de {@code UsernamePasswordAuthenticationFilter}
 * en {@link com.eventnode.eventnodeapi.config.SecurityConfig}, de modo que si hay JWT válido,
 * Spring Security ya dispone de un {@link org.springframework.security.core.Authentication} en el
 * {@link SecurityContextHolder} antes de llegar a los controladores.</p>
 * <p>Si no hay token o es inválido, la petición sigue sin autenticación previa; entonces solo pasarán
 * rutas marcadas como {@code permitAll()} o fallarán las que exigen usuario autenticado.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    /** Implementación concreta: {@link CustomUserDetailsService} (inyectada como interfaz). */
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Punto central del filtro: intenta autenticar por JWT y siempre delega al resto de la cadena.
     *
     * @param request     petición HTTP entrante (headers, ruta, etc.)
     * @param response    respuesta (no se modifica aquí en caso de token inválido; el {@code AccessDenied}
     *                    lo resuelve más adelante si la ruta está protegida)
     * @param filterChain cadena de filtros restante; <strong>debe</strong> invocarse {@code doFilter}
     *                    para no cortar la petición
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1) Extraer cadena JWT del header Authorization (si existe)
        String token = getTokenFromRequest(request);

        // 2) Solo si hay texto no vacío y la firma/expiración del token son válidas
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 3) El "subject" del JWT fue configurado en login como el correo del usuario
            String username = jwtTokenProvider.getUsername(token);
            // 4) Cargar contraseña (hash), roles y flags de cuenta desde la base de datos
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5) Token de autenticación de Spring: principal = UserDetails, credenciales null (ya validadas por JWT)
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // 6) IP, session id, etc. — útil para auditoría y expresiones de seguridad avanzadas
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 7) Hace visible el usuario autenticado para @PreAuthorize, SecurityContext, etc.
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        // Si no hubo token válido, no se toca SecurityContext (sigue anónimo hasta otro mecanismo)

        // 8) Continuar filtros y finalmente el DispatcherServlet → controlador
        filterChain.doFilter(request, response);
    }

    /**
     * Lee el header estándar {@code Authorization: Bearer &lt;jwt&gt;}.
     *
     * @param request petición actual
     * @return el JWT sin el prefijo {@code "Bearer "}, o {@code null} si no hay header usable
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer ".length() == 7
            return bearerToken.substring(7);
        }
        return null;
    }
}
