package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UsuarioRepository - Repositorio de Usuarios
 * 
 * ¿Qué es?: Es una interfaz de Spring Data JPA que actúa como puente entre la 
 * aplicación y la tabla 'usuarios' de la base de datos.
 * 
 * ¿Para qué sirve?: Permite realizar consultas automáticas a la base de datos 
 * sin escribir código SQL manualmente.
 * 
 * ¿Cómo funciona?: 
 * - Al extender de 'JpaRepository', hereda métodos como save(), findAll(), findById() y delete().
 * - Spring genera automáticamente la implementación de los métodos definidos por nombre 
 *   (ej. 'findByCorreo' genera un SELECT WHERE correo = ?).
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * Útil para el proceso de Login y validación de correos únicos.
     */
    Optional<Usuario> findByCorreo(String correo);

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasa si busco un correo que no existe?
     *    - Respuesta: Retornará un 'Optional.empty()', lo que permite al servicio 
     *      manejar la ausencia del usuario de forma segura sin errores de null.
     * 
     * 2. ¿Cómo agrego una búsqueda por apellido?
     *    - Respuesta: Solo agrega 'List<Usuario> findByApellidoPaterno(String apellido);' 
     *      y Spring hará la magia.
     * 
     * 3. ¿Dónde está la lógica SQL de esto?
     *    - Respuesta: Spring la genera en tiempo de ejecución. No necesitas verla ni escribirla.
     * 
     * 4. ¿Puedo borrar usuarios desde aquí?
     *    - Respuesta: Sí, usando el método heredado 'deleteById(Integer id)'.
     */
}

