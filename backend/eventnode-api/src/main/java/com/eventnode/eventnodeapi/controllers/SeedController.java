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
 * SeedController - Controlador de Inicialización de Datos (Seeding)
 * 
 * ¿Qué es?: Es un controlador especial utilizado para "sembrar" (seed) los datos 
 * iniciales necesarios para que el sistema funcione por primera vez.
 * 
 * ¿Para qué sirve?: Crea automáticamente los roles (ALUMNO, ADMINISTRADOR, SUPERADMIN) 
 * y la cuenta del Administrador Principal si no existen en la base de datos.
 * 
 * ¿Cómo funciona?: 
 * - Se ejecuta mediante una petición POST a '/api/seed/init'.
 * - Verifica la existencia de roles y los crea si faltan.
 * - Crea el usuario 'admin@eventnode.com' con una contraseña predeterminada.
 * - Asegura que la contraseña del administrador esté encriptada con BCrypt.
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
     * Inicializa la base de datos con roles y un superusuario.
     */
    @PostMapping("/init")
    @Transactional
    public ResponseEntity<Map<String, String>> seedInitialData() {
        Map<String, String> result = new HashMap<>();

        // 1. Crear roles básicos del sistema
        createRolIfNotExists("ALUMNO");
        createRolIfNotExists("ADMINISTRADOR");
        createRolIfNotExists("SUPERADMIN");

        // 2. Crear SuperAdmin maestro si no existe
        if (usuarioRepository.findByCorreo("admin@eventnode.com").isEmpty()) {
            Rol rolSuperAdmin = rolRepository.findByNombre("SUPERADMIN")
                    .orElseThrow(() -> new IllegalStateException("Rol SUPERADMIN no encontrado"));

            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellidoPaterno("EventNode");
            admin.setApellidoMaterno("Principal");
            admin.setCorreo("admin@eventnode.com");
            admin.setPassword(passwordEncoder.encode("Admin@1234")); // Password por defecto
            admin.setEstado("ACTIVO");
            admin.setIntentosFallidos(0);
            admin.setRol(rolSuperAdmin);
            admin.setFechaCreacion(LocalDateTime.now());

            Usuario saved = usuarioRepository.save(admin);

            // Relación con la tabla de Administradores
            Administrador administrador = new Administrador();
            administrador.setUsuario(saved);
            administrador.setEsPrincipal(true);
            administradorRepository.save(administrador);

            result.put("mensaje", "Datos iniciales creados exitosamente. SuperAdmin: admin@eventnode.com / Admin@1234");
        } else {
            // 3. Si ya existe, asegurar que el password esté actualizado con BCrypt
            Usuario admin = usuarioRepository.findByCorreo("admin@eventnode.com").get();
            admin.setPassword(passwordEncoder.encode("Admin@1234"));
            usuarioRepository.save(admin);
            result.put("mensaje", "Password de SuperAdmin actualizado con BCrypt. SuperAdmin: admin@eventnode.com / Admin@1234");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Método privado para evitar duplicidad de roles.
     */
    private void createRolIfNotExists(String nombre) {
        if (rolRepository.findByNombre(nombre).isEmpty()) {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rolRepository.save(rol);
        }
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si ejecuto este endpoint varias veces?
     *    - Respuesta: No pasa nada malo. El código verifica si los datos ya existen 
     *      antes de intentar crearlos, por lo que es seguro ("idempotente").
     * 
     * 2. ¿Este endpoint es público?
     *    - Respuesta: Sí, por ahora es público para facilitar la instalación inicial. 
     *      En producción, debería protegerse o eliminarse por seguridad.
     * 
     * 3. ¿Dónde está el botón para ejecutar esto?
     *    - Respuesta: No hay un botón en la interfaz. Se debe llamar manualmente 
     *      (ej. usando Postman o cURL) al desplegar la aplicación por primera vez.
     * 
     * 4. ¿Puedo cambiar la contraseña del admin inicial aquí?
     *    - Respuesta: Sí, cambiando el valor en 'passwordEncoder.encode("Admin@1234")'.
     */
}
