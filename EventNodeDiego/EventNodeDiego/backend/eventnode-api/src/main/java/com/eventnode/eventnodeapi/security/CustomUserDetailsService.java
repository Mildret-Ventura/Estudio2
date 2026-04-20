package com.eventnode.eventnodeapi.security;

import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Puente entre la tabla {@code usuarios} y el modelo de seguridad de Spring ({@link UserDetails}).
 * <p>Spring Security compara el rol con reglas {@code hasRole("ALUMNO")} internamente como
 * {@code ROLE_ALUMNO}; por eso se antepone {@code "ROLE_"} al nombre del rol en base de datos.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Invocado por el filtro JWT (y potencialmente por el {@code DaoAuthenticationProvider} si se usara
     * login por formulario) con el {@code subject} del token = correo electrónico.
     *
     * @param email correo único del usuario (no confundir con id numérico)
     * @return contraseña almacenada (BCrypt), flags de cuenta y una sola autoridad derivada del rol
     * @throws UsernameNotFoundException si no existe fila en {@code usuarios} con ese correo
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + email));

        // Convención Spring: hasRole("X") ↔ GrantedAuthority "ROLE_X"
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre());

        return new User(
                usuario.getCorreo(),
                usuario.getPassword(),
                "ACTIVO".equals(usuario.getEstado()), // enabled: INACTIVO → no puede autenticarse
                true, // accountNonExpired — no se modela vencimiento de cuenta en esta app
                true, // credentialsNonExpired — no se fuerza cambio de password por antigüedad aquí
                // accountNonLocked: bloqueo temporal tras intentos fallidos de login
                usuario.getBloqueadoHasta() == null || usuario.getBloqueadoHasta().isBefore(java.time.LocalDateTime.now()),
                Set.of(authority)
        );
    }
}
