package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Categoria;
import com.eventnode.eventnodeapi.models.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * EventoRepository - Repositorio de Eventos
 * 
 * ¿Qué es?: Es el componente de acceso a datos para la cartelera de eventos.
 * 
 * ¿Para qué sirve?: Permite realizar búsquedas complejas de eventos, 
 * validaciones de duplicados y estadísticas por categoría.
 * 
 * ¿Cómo funciona?: 
 * - Extiende de 'JpaSpecificationExecutor' para permitir filtrado dinámico 
 *   (búsqueda por nombre, mes, categoría al mismo tiempo).
 * - Provee métodos para validar que no haya dos eventos con el mismo nombre 
 *   a la misma hora.
 */
public interface EventoRepository extends JpaRepository<Evento, Integer>, JpaSpecificationExecutor<Evento> {

    /**
     * Busca un evento exacto por nombre y hora de inicio.
     */
    Optional<Evento> findByNombreAndFechaInicio(String nombre, LocalDateTime fechaInicio);

    /**
     * Valida si existe un evento duplicado (mismo nombre y fecha) 
     * excluyendo un ID específico (útil al editar).
     */
    boolean existsByNombreAndFechaInicioAndIdEventoNot(String nombre, LocalDateTime fechaInicio, Integer idEvento);

    /**
     * Cuenta cuántos eventos pertenecen a una categoría específica.
     */
    long countByCategoria(Categoria categoria);

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si quiero filtrar eventos por ciudad?
     *    - Respuesta: Si agregas la columna 'ciudad' al modelo Evento, 
     *      puedes agregar aquí 'List<Evento> findByCiudad(String ciudad);'.
     * 
     * 2. ¿Cómo funciona la búsqueda dinámica de la cartelera?
     *    - Respuesta: Se usa el JpaSpecificationExecutor, el cual recibe un objeto 
     *      'Specification' construido en el [EventoService.java].
     * 
     * 3. ¿Dónde está la lógica si piden un botón de "Eliminar todos los eventos pasados"?
     *    - Respuesta: Podrías agregar un método '@Modifying @Query("DELETE FROM Evento e WHERE e.fechaFin < :ahora")' 
     *      y llamarlo desde un botón en el panel de admin.
     */
}

