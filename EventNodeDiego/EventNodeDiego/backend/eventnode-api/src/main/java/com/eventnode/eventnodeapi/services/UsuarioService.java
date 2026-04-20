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
 * Operaciones sobre {@link Usuario} y proyección a {@link PerfilResponse} según el tipo de cuenta.
 * <p>Importante: el controlador de perfil no comprueba explícitamente que solo el dueño consulte su {@code id};
 * si se requiere aislamiento estricto, añadir comprobación de {@code SecurityContext} frente al {@code idUsuario}.</p>
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
     * Lista todos los usuarios del sistema proyectados a DTO de perfil.
     * <p>Coste O(n²) en el peor caso: por cada usuario se llama {@link #obtenerPerfil(Integer)} que hace nuevas lecturas.
     * Para muchos registros convendría una consulta agregada o proyección JPQL.</p>
     */
    public List<PerfilResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> obtenerPerfil(usuario.getIdUsuario()))
                .collect(Collectors.toList());
    }

    /**
     * Construye un {@link PerfilResponse} con campos base de {@link Usuario} y, según rol,
     * datos de {@link Alumno} o {@link Administrador}.
     *
     * @param idUsuario identificador numérico
     * @return DTO listo para serializar a JSON
     * @throws IllegalArgumentException si no existe el usuario
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
            adminOpt.ifPresent(admin -> perfil.setEsPrincipal(admin.getEsPrincipal()));
        }

        return perfil;
    }

    /**
     * Crea un usuario con rol {@code ADMINISTRADOR} y fila en {@code administradores} (nunca SUPERADMIN por esta vía).
     *
     * @param request incluye {@code idSolicitante}: debe ser ADMINISTRADOR o SUPERADMIN
     * @return perfil recién creado
     * @throws SecurityException        si el solicitante no es administrador
     * @throws IllegalArgumentException correo duplicado o validación de password
     * @throws IllegalStateException    si falta el rol ADMINISTRADOR en catálogo
     */
    @Transactional
    public PerfilResponse registrarAdmin(AdminRegistroRequest request) {
        Usuario solicitante = usuarioRepository.findById(request.getIdSolicitante())
                .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado"));

        String rolSolicitante = solicitante.getRol() != null ? solicitante.getRol().getNombre() : null;
        if (!"SUPERADMIN".equals(rolSolicitante) && !"ADMINISTRADOR".equals(rolSolicitante)) {
            throw new SecurityException("Solo un administrador puede crear administradores");
        }

        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico");
        }

        if (!request.getPassword().matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial");
        }

        Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR")
                .orElseThrow(() -> new IllegalStateException("Rol ADMINISTRADOR no encontrado en la base de datos"));

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

        Administrador admin = new Administrador();
        admin.setUsuario(savedUsuario);
        admin.setIdUsuario(savedUsuario.getIdUsuario());
        admin.setEsPrincipal(false);
        administradorRepository.save(admin);

        return obtenerPerfil(savedUsuario.getIdUsuario());
    }

    /**
     * Conmuta entre ACTIVO e INACTIVO (cualquier otro valor caería en la rama else y pasaría a ACTIVO).
     *
     * @param idUsuario usuario a modificar
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
     * Actualización parcial por mapa de JSON: solo se procesan claves presentes ({@code nombre}, {@code apellidoPaterno}, {@code apellidoMaterno}).
     *
     * @param idUsuario fila a modificar
     * @param datos     cuerpo JSON deserializado como mapa genérico (casting a String en runtime)
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
        if (datos.containsKey("apellidoPaterno")) {
            String ap = (String) datos.get("apellidoPaterno");
            if (ap == null || ap.trim().isEmpty()) {
                throw new IllegalArgumentException("El apellido paterno es obligatorio");
            }
            usuario.setApellidoPaterno(ap.trim());
        }
        if (datos.containsKey("apellidoMaterno")) {
            usuario.setApellidoMaterno(datos.get("apellidoMaterno") != null ? ((String) datos.get("apellidoMaterno")).trim() : null);
        }

        usuarioRepository.save(usuario);
        return obtenerPerfil(idUsuario);
    }
}
