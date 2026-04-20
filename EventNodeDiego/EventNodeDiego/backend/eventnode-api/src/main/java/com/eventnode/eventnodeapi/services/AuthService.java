package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.dtos.LoginRequest;
import com.eventnode.eventnodeapi.dtos.LoginResponse;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.models.Alumno;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import com.eventnode.eventnodeapi.repositories.AlumnoRepository;
import com.eventnode.eventnodeapi.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Lógica de negocio del inicio de sesión.
 * <p>Responsabilidades: localizar usuario, comprobar bloqueo temporal y estado ACTIVO, verificar contraseña
 * (hash BCrypt o texto plano legado), incrementar contador de fallos y bloquear a los 3 intentos,
 * migrar contraseñas legadas a BCrypt, emitir JWT con subject = correo, y adjuntar datos de {@link Alumno}
 * en la respuesta cuando el rol es ALUMNO.</p>
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioRepository usuarioRepository,
                       AlumnoRepository alumnoRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Flujo completo de autenticación para el endpoint {@code POST /api/auth/login}.
     *
     * @param request credenciales ya validadas a nivel de formato (email, no vacío)
     * @return DTO con token y datos de perfil para cachear en el cliente
     * @throws BadCredentialsException      correo inexistente o contraseña incorrecta
     * @throws LockedException              {@code bloqueado_hasta} aún no expiró
     * @throws DisabledException            estado distinto de ACTIVO
     */
    public LoginResponse login(LoginRequest request) {
        // Mismo mensaje para "no existe" y "password malo" → reduce enumeración de cuentas válidas
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        LocalDateTime ahora = LocalDateTime.now();

        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(ahora)) {
            throw new LockedException("Cuenta bloqueada, intente más tarde");
        }

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new DisabledException("Cuenta inactiva, contacte al administrador");
        }

        boolean passwordValid = false;
        boolean needsRehash = false;

        // BCrypt siempre empieza por "$2a$", "$2b$", etc.
        if (usuario.getPassword() != null && usuario.getPassword().startsWith("$2")) {
            passwordValid = passwordEncoder.matches(request.getPassword(), usuario.getPassword());
        } else {
            // Datos antiguos o de prueba: comparación directa; si coincide, se re-hashea al guardar
            passwordValid = request.getPassword().equals(usuario.getPassword());
            if (passwordValid) {
                needsRehash = true;
            }
        }

        if (!passwordValid) {
            int intentos = usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos();
            intentos++;
            usuario.setIntentosFallidos(intentos);

            if (intentos >= 3) {
                usuario.setBloqueadoHasta(ahora.plusMinutes(15));
            }

            usuarioRepository.save(usuario);
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        if (needsRehash) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // El filtro JWT solo necesita el "nombre" = correo; authorities se recargan desde BD al validar cada petición
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario.getCorreo(), null, null
        );
        String token = jwtTokenProvider.generateToken(authentication);

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : null;

        String matricula = null;
        String sexo = null;
        Integer cuatrimestre = null;

        // La app móvil puede mostrar matrícula en cabecera sin segunda petición
        if ("ALUMNO".equalsIgnoreCase(rolNombre)) {
            Optional<Alumno> alumnoOpt = alumnoRepository.findById(usuario.getIdUsuario());
            if (alumnoOpt.isPresent()) {
                Alumno alumno = alumnoOpt.get();
                matricula = alumno.getMatricula();
                sexo = alumno.getSexo();
                cuatrimestre = alumno.getCuatrimestre();
            }
        }

        return new LoginResponse(
                "Inicio de sesión exitoso",
                rolNombre,
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellidoPaterno(),
                usuario.getApellidoMaterno(),
                usuario.getCorreo(),
                matricula,
                sexo,
                cuatrimestre,
                token
        );
    }
}
