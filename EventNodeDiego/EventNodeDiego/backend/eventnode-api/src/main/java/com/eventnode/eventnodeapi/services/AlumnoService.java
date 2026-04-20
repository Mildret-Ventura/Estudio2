package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.dtos.AlumnoRegistroRequest;
import com.eventnode.eventnodeapi.models.Alumno;
import com.eventnode.eventnodeapi.models.Rol;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.AlumnoRepository;
import com.eventnode.eventnodeapi.repositories.RolRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Reglas de negocio para el ciclo de vida de un {@link Alumno} y su {@link Usuario} asociado (misma PK {@code id_usuario}).
 */
@Service
public class AlumnoService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /** Política de contraseña alineada con el mensaje de error mostrado al usuario. */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$"
    );

    public AlumnoService(UsuarioRepository usuarioRepository,
                         AlumnoRepository alumnoRepository,
                         RolRepository rolRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crea en una sola transacción la fila en {@code usuarios} (rol ALUMNO) y en {@code alumnos}.
     *
     * @param request datos validados por Bean Validation en el controlador
     * @throws IllegalStateException    correo o matrícula duplicados, o rol ALUMNO ausente en BD
     * @throws IllegalArgumentException contraseña débil, edad fuera de 17–99 o cuatrimestre fuera de 1–10
     */
    @Transactional
    public void registrarAlumno(AlumnoRegistroRequest request) {
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()
                || alumnoRepository.findByMatricula(request.getMatricula()).isPresent()) {
            throw new IllegalStateException("Matrícula o correo ya registrados");
        }

        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new IllegalArgumentException("La contraseña no es válida, debe tener mínimo 8 caracteres, incluir mayúsculas, minúsculas, números y un símbolo");
        }

        LocalDate fechaNac = request.getFechaNacimiento();
        int edad = Period.between(fechaNac, LocalDate.now()).getYears();
        if (edad < 17 || edad > 99) {
            throw new IllegalArgumentException("La edad ingresada no es válida para el registro académico");
        }

        Integer cuatrimestre = request.getCuatrimestre();
        if (cuatrimestre == null || cuatrimestre < 1 || cuatrimestre > 10) {
            throw new IllegalArgumentException("Cuatrimestre fuera de rango");
        }

        Rol rolAlumno = rolRepository.findByNombre("ALUMNO")
                .orElseThrow(() -> new IllegalStateException("Rol ALUMNO no configurado en el sistema"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado("ACTIVO");
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuario.setRol(rolAlumno);
        usuario.setFechaCreacion(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Alumno alumno = new Alumno();
        alumno.setIdUsuario(usuarioGuardado.getIdUsuario());
        alumno.setMatricula(request.getMatricula());
        alumno.setFechaNac(fechaNac);
        alumno.setEdad(edad);
        alumno.setSexo(request.getSexo());
        alumno.setCuatrimestre(cuatrimestre);
        alumno.setUsuario(usuarioGuardado);

        alumnoRepository.save(alumno);
    }

    /**
     * Actualiza datos editables del alumno y del usuario (nombres, correo, sexo, cuatrimestre, edad).
     * No cambia contraseña ni matrícula desde este método.
     *
     * @param idUsuario PK compartida usuario/alumno (debe coincidir con el token en un diseño estricto de API)
     * @param request   campos validados; correo único salvo que sea el mismo usuario
     */
    @Transactional
    public void actualizarAlumno(Integer idUsuario, com.eventnode.eventnodeapi.dtos.AlumnoActualizarRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Alumno alumno = alumnoRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));

        if (!usuario.getCorreo().equals(request.getCorreo())
                && usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new IllegalStateException("El correo ya está en uso");
        }

        Integer cuatrimestre = request.getCuatrimestre();
        if (cuatrimestre == null || cuatrimestre < 1 || cuatrimestre > 10) {
            throw new IllegalArgumentException("Cuatrimestre fuera de rango");
        }

        Integer edad = request.getEdad();
        if (edad == null || edad < 17 || edad > 99) {
            throw new IllegalArgumentException("Edad fuera de rango");
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setCorreo(request.getCorreo());

        alumno.setSexo(request.getSexo());
        alumno.setCuatrimestre(cuatrimestre);
        alumno.setEdad(edad);

        usuarioRepository.save(usuario);
        alumnoRepository.save(alumno);
    }
}
