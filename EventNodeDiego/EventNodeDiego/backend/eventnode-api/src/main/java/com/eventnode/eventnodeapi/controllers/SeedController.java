package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.models.Administrador;
import com.eventnode.eventnodeapi.models.Rol;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.AdministradorRepository;
import com.eventnode.eventnodeapi.repositories.RolRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint de desarrollo para crear roles (ALUMNO, ADMINISTRADOR, SUPERADMIN) y un usuario superadmin inicial.
 * <p><strong>Solo debe usarse en entornos controlados;</strong> en producción deshabilitar o proteger.</p>
 */
@RestController
@RequestMapping("/api/seed")
public class SeedController {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedController(RolRepository rolRepository,
                          UsuarioRepository usuarioRepository,
                          AdministradorRepository administradorRepository,
                          PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Idempotente en roles: los crea si faltan. Usuario demo {@code admin@eventnode.com} se crea una vez
     * o solo se refresca el hash BCrypt de la contraseña si ya existía.
     */
    @PostMapping("/init")
    @Transactional
    public ResponseEntity<Map<String, String>> seedInitialData() {
        Map<String, String> result = new HashMap<>();

        // Crear roles si no existen
        createRolIfNotExists("ALUMNO");
        createRolIfNotExists("ADMINISTRADOR");
        createRolIfNotExists("SUPERADMIN");

        // Crear SuperAdmin maestro si no existe
        if (usuarioRepository.findByCorreo("admin@eventnode.com").isEmpty()) {
            Rol rolSuperAdmin = rolRepository.findByNombre("SUPERADMIN")
                    .orElseThrow(() -> new IllegalStateException("Rol SUPERADMIN no encontrado"));

            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellidoPaterno("EventNode");
            admin.setApellidoMaterno("Principal");
            admin.setCorreo("admin@eventnode.com");
            admin.setPassword(passwordEncoder.encode("Admin@1234"));
            admin.setEstado("ACTIVO");
            admin.setIntentosFallidos(0);
            admin.setRol(rolSuperAdmin);
            admin.setFechaCreacion(LocalDateTime.now());

            Usuario saved = usuarioRepository.save(admin);

            Administrador administrador = new Administrador();
            administrador.setUsuario(saved);
            administrador.setEsPrincipal(true);
            administradorRepository.save(administrador);

            result.put("mensaje", "Datos iniciales creados exitosamente. SuperAdmin: admin@eventnode.com / Admin@1234");
        } else {
            // Asegurar que el password esté hasheado con BCrypt incluso si ya existe
            Usuario admin = usuarioRepository.findByCorreo("admin@eventnode.com").get();
            admin.setPassword(passwordEncoder.encode("Admin@1234"));
            usuarioRepository.save(admin);
            result.put("mensaje", "Password de SuperAdmin actualizado con BCrypt. SuperAdmin: admin@eventnode.com / Admin@1234");
        }

        return ResponseEntity.ok(result);
    }

    /** Inserta rol por nombre si no hay fila con ese {@code nombre} único. */
    private void createRolIfNotExists(String nombre) {
        if (rolRepository.findByNombre(nombre).isEmpty()) {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rolRepository.save(rol);
        }
    }
}
