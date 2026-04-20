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
 * AuthService - Servicio de Autenticación
 * 
 * ¿Qué es?: Es un servicio (@Service) que contiene la lógica de negocio para validar usuarios,
 * manejar intentos fallidos, bloquear cuentas y generar tokens de acceso.
 * 
 * ¿Para qué sirve?: Centraliza la seguridad del inicio de sesión, asegurando que solo usuarios
 * válidos y activos puedan entrar al sistema.
 * 
 * ¿Cómo funciona?: 
 * 1. Busca al usuario por correo.
 * 2. Verifica si la cuenta está bloqueada o inactiva.
 * 3. Compara la contraseña (soporta contraseñas antiguas en texto plano y nuevas con BCrypt).
 * 4. Si es válida, genera un token JWT y limpia los intentos fallidos.
 * 5. Si es inválida, aumenta el contador de intentos y bloquea si llega a 3.
 */
@Service
public class AuthService {

    // Repositorios para acceder a los datos de Usuario y Alumno
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
     * Proceso principal de login.
     * Valida credenciales y retorna toda la información necesaria para la sesión del frontend.
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Buscar usuario
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        LocalDateTime ahora = LocalDateTime.now();

        // 2. Verificar bloqueos temporales
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(ahora)) {
            throw new LockedException("Cuenta bloqueada, intente más tarde");
        }

        // 3. Verificar si la cuenta fue desactivada por un administrador
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new DisabledException("Cuenta inactiva, contacte al administrador");
        }

        boolean passwordValid = false;
        boolean needsRehash = false;

        // 4. Validación de contraseña (Migración automática a BCrypt)
        if (usuario.getPassword() != null && usuario.getPassword().startsWith("$2")) {
            // Contraseña ya está encriptada
            passwordValid = passwordEncoder.matches(request.getPassword(), usuario.getPassword());
        } else {
            // Contraseña antigua en texto plano (se migra al primer login exitoso)
            passwordValid = request.getPassword().equals(usuario.getPassword());
            if (passwordValid) {
                needsRehash = true;
            }
        }

        // 5. Manejo de intentos fallidos
        if (!passwordValid) {
            int intentos = usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos();
            intentos++;
            usuario.setIntentosFallidos(intentos);

            // Bloqueo automático tras 3 errores
            if (intentos >= 3) {
                usuario.setBloqueadoHasta(ahora.plusMinutes(15));
            }

            usuarioRepository.save(usuario);
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        // 6. Encriptar contraseña si era texto plano
        if (needsRehash) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 7. Generar token JWT para sesiones futuras
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario.getCorreo(), null, null
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // 8. Limpiar estado de errores tras login exitoso
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        // 9. Recopilar datos adicionales del perfil (si es Alumno)
        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : null;
        String matricula = null;
        String sexo = null;
        Integer cuatrimestre = null;

        if ("ALUMNO".equalsIgnoreCase(rolNombre)) {
            Optional<Alumno> alumnoOpt = alumnoRepository.findById(usuario.getIdUsuario());
            if (alumnoOpt.isPresent()) {
                Alumno alumno = alumnoOpt.get();
                matricula = alumno.getMatricula();
                sexo = alumno.getSexo();
                cuatrimestre = alumno.getCuatrimestre();
            }
        }

        // Retornar respuesta completa
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

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A) SOBRE ESTE CÓDIGO:
     * 
     * 1. ¿Qué pasaría si un usuario intenta loguearse y su contraseña es null?
     *    - Respuesta: El código maneja esto en la línea que revisa si empieza con "$2". 
     *      Si es null, 'passwordValid' quedará en false y se contará como intento fallido.
     * 
     * 2. ¿Cuánto tiempo dura el bloqueo de cuenta?
     *    - Respuesta: Está configurado para durar 15 minutos (ahora.plusMinutes(15)).
     * 
     * 3. ¿Dónde está la lógica si piden cambiar un botón de "Cerrar Sesión"?
     *    - Respuesta: El cierre de sesión es puramente del frontend. Al ser una API con JWT, 
     *      el servidor no guarda sesiones. Para "cerrar sesión", el frontend simplemente borra 
     *      el token del localStorage. El componente es [CerrarSesionModal.jsx].
     * 
     * 4. ¿Qué pasaría si el rol del usuario no es "ALUMNO"?
     *    - Respuesta: El código simplemente no buscará en la tabla de alumnos y los campos 
     *      como 'matricula' o 'cuatrimestre' se enviarán como null en la respuesta.
     */
}
