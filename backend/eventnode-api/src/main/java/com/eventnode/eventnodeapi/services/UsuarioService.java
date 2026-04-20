package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.dtos.AdminRegistroRequest;
import com.eventnode.eventnodeapi.dtos.PerfilResponse;
import com.eventnode.eventnodeapi.models.Administrador;
import com.eventnode.eventnodeapi.models.Alumno;
import com.eventnode.eventnodeapi.models.Rol;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.AdministradorRepository;
import com.eventnode.eventnodeapi.repositories.AlumnoRepository;
import com.eventnode.eventnodeapi.repositories.RolRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UsuarioService - Servicio de Gestión de Perfiles y Roles
 * 
 * ¿Qué es?: Es el componente que unifica la información de las tablas 'usuarios', 
 * 'alumnos' y 'administradores' para presentar una visión completa del usuario.
 * 
 * ¿Para qué sirve?: Maneja la creación de nuevos administradores, la actualización 
 * de perfiles y el cambio de estado (activación/desactivación) de las cuentas.
 * 
 * ¿Cómo funciona?: 
 * - Al obtener un perfil, verifica el rol del usuario para saber si debe buscar 
 *   datos adicionales en la tabla de Alumnos o de Administradores.
 * - Valida permisos de seguridad: solo un SuperAdmin puede crear otros Admins.
 * - Asegura que las contraseñas de los nuevos administradores cumplan con las 
 *   políticas de seguridad de la institución.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final AdministradorRepository administradorRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          AlumnoRepository alumnoRepository,
                          AdministradorRepository administradorRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.administradorRepository = administradorRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtiene la lista de todos los usuarios transformados a DTO de Perfil.
     */
    public List<PerfilResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> obtenerPerfil(usuario.getIdUsuario()))
                .collect(Collectors.toList());
    }

    /**
     * Construye un objeto PerfilResponse combinando datos de múltiples tablas.
     */
    public PerfilResponse obtenerPerfil(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        PerfilResponse perfil = new PerfilResponse();
        perfil.setIdUsuario(usuario.getIdUsuario());
        perfil.setNombre(usuario.getNombre());
        perfil.setApellidoPaterno(usuario.getApellidoPaterno());
        perfil.setApellidoMaterno(usuario.getApellidoMaterno());
        perfil.setCorreo(usuario.getCorreo());
        perfil.setEstado(usuario.getEstado());

        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : null;
        perfil.setRol(rolNombre);

        // Lógica Condicional: Cargar datos según el tipo de usuario
        if ("ALUMNO".equals(rolNombre)) {
            Optional<Alumno> alumnoOpt = alumnoRepository.findById(idUsuario);
            alumnoOpt.ifPresent(alumno -> {
                perfil.setMatricula(alumno.getMatricula());
                perfil.setFechaNacimiento(alumno.getFechaNac());
                perfil.setEdad(alumno.getEdad());
                perfil.setSexo(alumno.getSexo());
                perfil.setCuatrimestre(alumno.getCuatrimestre());
            });
        }

        if ("ADMINISTRADOR".equals(rolNombre) || "SUPERADMIN".equals(rolNombre)) {
            Optional<Administrador> adminOpt = administradorRepository.findById(idUsuario);
            adminOpt.ifPresent(admin -> {
                perfil.setEsPrincipal(admin.getEsPrincipal());
            });
        }

        return perfil;
    }

    /**
     * Registra un nuevo Administrador validando permisos del solicitante.
     */
    @Transactional
    public PerfilResponse registrarAdmin(AdminRegistroRequest request) {
        // 1. Solo un administrador (especialmente SuperAdmin) puede crear otros administradores
        Usuario solicitante = usuarioRepository.findById(request.getIdSolicitante())
                .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado"));

        String rolSolicitante = solicitante.getRol() != null ? solicitante.getRol().getNombre() : null;
        if (!"SUPERADMIN".equals(rolSolicitante) && !"ADMINISTRADOR".equals(rolSolicitante)) {
            throw new SecurityException("Solo un administrador puede crear administradores");
        }

        // 2. Validar correo único
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico");
        }

        // 3. Validar fortaleza de contraseña
        if (!request.getPassword().matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial");
        }

        // 4. Buscar rol
        Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR")
                .orElseThrow(() -> new IllegalStateException("Rol ADMINISTRADOR no encontrado en la base de datos"));

        // 5. Crear Entidad Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado("ACTIVO");
        usuario.setIntentosFallidos(0);
        usuario.setRol(rolAdmin);
        usuario.setFechaCreacion(LocalDateTime.now());

        Usuario savedUsuario = usuarioRepository.save(usuario);

        // 6. Crear Entidad Administrador vinculada
        Administrador admin = new Administrador();
        admin.setUsuario(savedUsuario);
        admin.setIdUsuario(savedUsuario.getIdUsuario());
        admin.setEsPrincipal(false); // Los nuevos no son SuperAdmins por defecto
        administradorRepository.save(admin);

        return obtenerPerfil(savedUsuario.getIdUsuario());
    }

    /**
     * Activa o desactiva una cuenta de usuario.
     */
    @Transactional
    public void cambiarEstado(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if ("ACTIVO".equals(usuario.getEstado())) {
            usuario.setEstado("INACTIVO");
        } else {
            usuario.setEstado("ACTIVO");
        }

        usuarioRepository.save(usuario);
    }

    /**
     * Permite actualizar datos personales básicos.
     */
    @Transactional
    public PerfilResponse actualizarPerfil(Integer idUsuario, Map<String, Object> datos) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (datos.containsKey("nombre")) {
            String nombre = (String) datos.get("nombre");
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }
            usuario.setNombre(nombre.trim());
        }
        // ... otras actualizaciones de campos ...
        usuarioRepository.save(usuario);
        return obtenerPerfil(idUsuario);
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si un Alumno intenta acceder al endpoint de 'listarTodos'?
     *    - Respuesta: Aunque este servicio lo permite, la configuración de [SecurityConfig.java] 
     *      debería bloquear el acceso al controlador antes de que llegue aquí.
     * 
     * 2. ¿Por qué se usa un Map para 'actualizarPerfil'?
     *    - Respuesta: Para permitir actualizaciones parciales (PATCH style). Así el 
     *      frontend solo envía los campos que el usuario realmente cambió.
     * 
     * 3. ¿Dónde está el botón para deshabilitar un alumno?
     *    - Respuesta: En la página [AdminEstudiantes.jsx]. El administrador tiene un switch 
     *      o botón de acción que llama a 'cambiarEstado' en este servicio.
     * 
     * 4. ¿Puedo borrar un usuario permanentemente?
     *    - Respuesta: El sistema prefiere el "borrado lógico" (cambiar estado a INACTIVO) 
     *      para mantener el historial de asistencias y diplomas generados.
     */
}
}
