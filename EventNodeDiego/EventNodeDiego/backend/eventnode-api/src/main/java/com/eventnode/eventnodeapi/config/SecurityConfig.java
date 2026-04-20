package com.eventnode.eventnodeapi.config;

import com.eventnode.eventnodeapi.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de Spring Security para la API REST.
 * <ul>
 *   <li><strong>Stateless:</strong> no hay sesión HTTP ni cookies de login; la identidad va en JWT.</li>
 *   <li><strong>CSRF desactivado:</strong> típico en APIs que no usan sesión de navegador con cookies same-site.</li>
 *   <li><strong>Orden de matchers:</strong> la primera regla que coincide gana; {@code anyRequest()} va al final.</li>
 *   <li>Rutas no listadas explícitamente quedan en {@code authenticated()} — requieren JWT salvo {@code permitAll}.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Define la cadena de filtros: CORS, autorización por URL y registro del filtro JWT.
     *
     * @param http builder fluido de Spring Security 6.x (lambda DSL)
     * @return cadena terminada e inmutable para el contenedor
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Navegadores en otro origen (Vite) pueden llamar la API si el origen está en la lista permitida
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            // Sin HttpSession: cada petición autenticada debe llevar Authorization Bearer
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // --- Sin token ---
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/alumnos/registro").permitAll()
                .requestMatchers("/api/seed/init").permitAll()

                // Categorías: lectura pública; escritura solo staff
                .requestMatchers(HttpMethod.GET, "/api/categorias").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/categorias").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")

                // Eventos: calendario/detalles públicos; mutaciones solo administración
                .requestMatchers(HttpMethod.GET, "/api/eventos/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/eventos/crear").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/eventos/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/eventos/*/cancelar").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/eventos/*/reactivar").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")

                // Validar/rechazar asistencias pendientes (QR) — solo staff
                .requestMatchers(HttpMethod.PATCH, "/api/asistencias/*/estado").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")

                // Pre-checkin: alumno gestiona su inscripción; listados de evento para staff
                .requestMatchers("/api/precheckin/inscribirse").hasRole("ALUMNO")
                .requestMatchers("/api/precheckin/cancelar").hasRole("ALUMNO")
                .requestMatchers("/api/precheckin/usuario/**").hasRole("ALUMNO")
                .requestMatchers("/api/precheckin/evento/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")

                // Usuarios: listado y alta admin restringidos; perfil cualquier usuario autenticado (debe validarse en servicio que sea el propio o admin)
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/usuarios/*/perfil").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/perfil").authenticated()
                .requestMatchers("/api/usuarios/admin").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")

                // Diplomas, asistencias POST, etc.: no coincidieron reglas anteriores → JWT obligatorio
                .anyRequest().authenticated()
            );

        // El filtro lee Authorization, valida JWT y rellena SecurityContext antes del resto de la cadena
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Expuesto por si en el futuro se usa {@code AuthenticationManager} en un endpoint de login explícito;
     * hoy {@link com.eventnode.eventnodeapi.services.AuthService} valida credenciales manualmente.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * CORS explícito: orígenes de desarrollo front-end. {@code allowCredentials(true)} requiere orígenes
     * concretos (no {@code *}) cuando el cliente envía cookies o Authorization desde el navegador.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Algoritmo BCrypt para hashes de contraseña (registro, login, recuperación).
     * Fuerza de trabajo por defecto del encoder es adecuada para entornos web.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

