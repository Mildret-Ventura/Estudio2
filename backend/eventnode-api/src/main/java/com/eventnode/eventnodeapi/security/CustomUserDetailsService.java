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

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + email));

        // En Spring Security, el nombre de la autoridad por convención suele ser ROLE_ + nombre
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre());

        return new User(
                usuario.getCorreo(),
                usuario.getPassword(),
                "ACTIVO".equals(usuario.getEstado()), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                usuario.getBloqueadoHasta() == null || usuario.getBloqueadoHasta().isBefore(java.time.LocalDateTime.now()), // accountNonLocked
                Set.of(authority)
        );
    }
}
