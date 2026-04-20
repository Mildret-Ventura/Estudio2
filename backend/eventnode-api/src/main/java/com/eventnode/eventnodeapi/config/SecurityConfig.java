package com.eventnode.eventnodeapi.config;

import com.eventnode.eventnodeapi.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints Públicos
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/alumnos/registro").permitAll()
                .requestMatchers("/api/seed/init").permitAll()
                
                // Endpoints de Categorías
                .requestMatchers(HttpMethod.GET, "/api/categorias").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/categorias").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")

                // Endpoints de Eventos
                .requestMatchers(HttpMethod.GET, "/api/eventos/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/eventos/crear").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/eventos/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/eventos/*/cancelar").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/eventos/*/reactivar").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                
                // Endpoints de Asistencias
                .requestMatchers(HttpMethod.PATCH, "/api/asistencias/*/estado").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")

                // Endpoints de Inscripciones (PreCheckin)
                .requestMatchers("/api/precheckin/inscribirse").hasRole("ALUMNO")
                .requestMatchers("/api/precheckin/cancelar").hasRole("ALUMNO")
                .requestMatchers("/api/precheckin/usuario/**").hasRole("ALUMNO")
                .requestMatchers("/api/precheckin/evento/**").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                
                // Endpoints de Usuarios
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/usuarios/*/perfil").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/perfil").authenticated()
                .requestMatchers("/api/usuarios/admin").hasAnyRole("ADMINISTRADOR", "SUPERADMIN")
                
                // Cualquier otra petición necesita autenticación
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

