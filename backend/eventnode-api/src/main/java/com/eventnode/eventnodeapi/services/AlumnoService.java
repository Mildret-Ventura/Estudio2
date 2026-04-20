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
 * AlumnoService - Servicio de Lógica de Alumnos
 * 
 * ¿Qué es?: Es la capa de servicios encargada de validar y procesar la información 
 * de los estudiantes antes de guardarla en la base de datos.
 * 
 * ¿Para qué sirve?: Garantiza que los alumnos cumplan con los requisitos del sistema 
 * (edad, formato de contraseña, unicidad de matrícula) y coordina la creación de 
 * registros en dos tablas distintas: 'usuarios' y 'alumnos'.
 * 
 * ¿Cómo funciona?: 
 * - Valida la fortaleza de la contraseña mediante una Expresión Regular (Regex).
 * - Calcula la edad del alumno basándose en su fecha de nacimiento.
 * - Utiliza transacciones (@Transactional) para asegurar que si falla la creación 
 *   del registro de 'alumno', también se deshaga la creación del 'usuario'.
 */
@Service
public class AlumnoService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // Expresión regular para validar: Mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 símbolo
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
     * Proceso de registro de un nuevo alumno.
     * Crea un Usuario (base) y un Alumno (perfil específico).
     */
    @Transactional
    public void registrarAlumno(AlumnoRegistroRequest request) {
        // 1. Validar que no existan duplicados
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()
                || alumnoRepository.findByMatricula(request.getMatricula()).isPresent()) {
            throw new IllegalStateException("Matrícula o correo ya registrados");
        }

        // 2. Validar fortaleza de contraseña
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new IllegalArgumentException("La contraseña no es válida, debe tener mínimo 8 caracteres, incluir mayúsculas, minúsculas, números y un símbolo");
        }

        // 3. Validar edad (Mínimo 17 años para universitarios)
        LocalDate fechaNac = request.getFechaNacimiento();
        int edad = Period.between(fechaNac, LocalDate.now()).getYears();
        if (edad < 17 || edad > 99) {
            throw new IllegalArgumentException("La edad ingresada no es válida para el registro académico");
        }

        // 4. Validar rango de cuatrimestre
        Integer cuatrimestre = request.getCuatrimestre();
        if (cuatrimestre == null || cuatrimestre < 1 || cuatrimestre > 10) {
            throw new IllegalArgumentException("Cuatrimestre fuera de rango");
        }

        // 5. Obtener el rol de ALUMNO
        Rol rolAlumno = rolRepository.findByNombre("ALUMNO")
                .orElseThrow(() -> new IllegalStateException("Rol ALUMNO no configurado en el sistema"));

        // 6. Crear y guardar la entidad Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword())); // Encriptar contraseña
        usuario.setEstado("ACTIVO");
        usuario.setIntentosFallidos(0);
        usuario.setRol(rolAlumno);
        usuario.setFechaCreacion(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 7. Crear y guardar la entidad Alumno vinculada al Usuario
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
     * Actualiza la información del perfil del alumno.
     */
    @Transactional
    public void actualizarAlumno(Integer idUsuario, com.eventnode.eventnodeapi.dtos.AlumnoActualizarRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                
        Alumno alumno = alumnoRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));

        // Validar si el nuevo correo ya lo tiene alguien más
        if (!usuario.getCorreo().equals(request.getCorreo()) && 
            usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
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

        // Actualizar datos en ambas tablas
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

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si el rol 'ALUMNO' no existe en la base de datos?
     *    - Respuesta: Se lanzará una IllegalStateException y el registro se cancelará. 
     *      Para evitar esto, se debe ejecutar primero el [SeedController.java].
     * 
     * 2. ¿Por qué se guarda en dos tablas?
     *    - Respuesta: Porque un 'Usuario' contiene datos generales (login, nombre) 
     *      y un 'Alumno' contiene datos específicos de la escuela (matrícula, cuatrimestre).
     * 
     * 3. ¿Cómo se calcula la edad automáticamente?
     *    - Respuesta: Usando 'Period.between' que compara la fecha de nacimiento 
     *      proporcionada contra la fecha actual del servidor.
     * 
     * 4. ¿Qué pasa si el registro de Usuario tiene éxito pero el de Alumno falla?
     *    - Respuesta: Gracias a @Transactional, Spring hará un "Rollback" automático y 
     *      borrará el Usuario recién creado para no dejar datos inconsistentes.
     */
}

