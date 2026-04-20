package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.dtos.EventoCreateRequest;
import com.eventnode.eventnodeapi.dtos.EventoUpdateRequest;
import com.eventnode.eventnodeapi.models.Categoria;
import com.eventnode.eventnodeapi.models.Evento;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.repositories.CategoriaRepository;
import com.eventnode.eventnodeapi.repositories.EventoRepository;
import com.eventnode.eventnodeapi.repositories.OrganizadorRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * EventoService - Servicio de Gestión de Eventos
 * 
 * ¿Qué es?: Es la capa de lógica de negocio donde se procesan todas las reglas para crear, 
 * actualizar, cancelar y consultar eventos.
 * 
 * ¿Para qué sirve?: Asegura que los datos de los eventos sean válidos antes de guardarlos 
 * (ej. que la fecha de inicio sea futura) y maneja las relaciones complejas entre tablas.
 * 
 * ¿Cómo funciona?: 
 * - Valida permisos del creador (debe ser ADMIN).
 * - Verifica coherencia de fechas y capacidades.
 * - Utiliza transacciones (@Transactional) para asegurar que si algo falla al guardar 
 *   asociaciones (como organizadores), no se guarde nada a medias.
 */
@Service
public class EventoService {

    // Dependencias inyectadas para interactuar con la DB
    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;
    private final OrganizadorRepository organizadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntityManager entityManager;

    public EventoService(EventoRepository eventoRepository,
                         CategoriaRepository categoriaRepository,
                         OrganizadorRepository organizadorRepository,
                         UsuarioRepository usuarioRepository,
                         EntityManager entityManager) {
        this.eventoRepository = eventoRepository;
        this.categoriaRepository = categoriaRepository;
        this.organizadorRepository = organizadorRepository;
        this.usuarioRepository = usuarioRepository;
        this.entityManager = entityManager;
    }

    /**
     * Lógica para crear un nuevo evento.
     * Incluye validaciones estrictas de negocio.
     */
    @Transactional
    public void crearEvento(EventoCreateRequest request) {

        // 1. Validar que el creador existe y tiene permisos de administrador
        Usuario creador = usuarioRepository.findById(request.getIdCreador())
                .orElseThrow(() -> new IllegalArgumentException("Usuario creador no encontrado"));

        String rolCreador = creador.getRol() != null ? creador.getRol().getNombre() : null;
        if (!"ADMINISTRADOR".equals(rolCreador) && !"SUPERADMIN".equals(rolCreador)) {
            throw new SecurityException("Solo los administradores pueden crear eventos");
        }

        // 2. Evitar duplicados por nombre y fecha
        if (eventoRepository.findByNombreAndFechaInicio(request.getNombre(), request.getFechaInicio()).isPresent()) {
            throw new IllegalStateException("Ya existe un evento con ese nombre en ese horario");
        }

        // 3. Validar coherencia temporal
        LocalDateTime ahora = LocalDateTime.now();
        if (!request.getFechaInicio().isAfter(ahora)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser posterior a la fecha y hora actual");
        }
        if (!request.getFechaFin().isAfter(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        // 4. Validar capacidad y tiempos
        if (request.getCapacidadMaxima() == null || request.getCapacidadMaxima() <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser un número mayor a cero");
        }

        if (request.getTiempoCancelacionHoras() == null || request.getTiempoCancelacionHoras() <= 0) {
            throw new IllegalArgumentException("El tiempo de aceptación de cancelación debe ser mayor a cero");
        }
        
        // El tiempo para cancelar debe ser menor al tiempo que falta para que inicie el evento
        long horasDisponibles = Duration.between(ahora, request.getFechaInicio()).toHours();
        if (request.getTiempoCancelacionHoras() > horasDisponibles) {
            throw new IllegalArgumentException("El tiempo de aceptación de cancelación no puede ser mayor al tiempo disponible antes del evento");
        }

        if (request.getTiempoToleranciaMinutos() == null || request.getTiempoToleranciaMinutos() < 0) {
            throw new IllegalArgumentException("El tiempo de tolerancia debe ser un número mayor o igual a cero");
        }

        // 5. Validar formato de imagen del banner (Base64)
        String banner = request.getBanner();
        if (banner != null && !banner.isBlank()) {
            if (!banner.startsWith("data:image/")) {
                throw new IllegalArgumentException("El banner debe ser una imagen válida (PNG, JPG, JPEG)");
            }
        }

        // 6. Buscar categoría
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // 7. Mapear DTO a Modelo y Guardar
        Evento evento = new Evento();
        evento.setNombre(request.getNombre());
        evento.setDescripcion(request.getDescripcion());
        evento.setUbicacion(request.getUbicacion());
        evento.setCapacidadMaxima(request.getCapacidadMaxima());
        evento.setFechaInicio(request.getFechaInicio());
        evento.setFechaFin(request.getFechaFin());
        evento.setTiempoCancelacionHoras(request.getTiempoCancelacionHoras());
        evento.setTiempoToleranciaMinutos(request.getTiempoToleranciaMinutos());
        evento.setBanner(banner != null && !banner.isBlank() ? banner : null);
        evento.setEstado("PRÓXIMO");
        evento.setCategoria(categoria);
        evento.setCreadoPor(request.getIdCreador());
        evento.setFechaCreacion(LocalDateTime.now());

        Evento saved = eventoRepository.save(evento);

        // 8. Insertar asociaciones con organizadores en la tabla intermedia (Muchos a Muchos)
        List<Integer> organizadorIds = request.getOrganizadores();
        if (organizadorIds != null && !organizadorIds.isEmpty()) {
            for (Integer idOrg : organizadorIds) {
                entityManager.createNativeQuery(
                    "INSERT INTO evento_organizador (id_evento, id_organizador) VALUES (:idEvento, :idOrg)")
                    .setParameter("idEvento", saved.getIdEvento())
                    .setParameter("idOrg", idOrg)
                    .executeUpdate();
            }
        }
    }

    /**
     * Consulta eventos usando filtros dinámicos.
     */
    @Transactional(readOnly = true)
    public List<Evento> consultarEventosDisponibles(String nombre, Integer mes, Integer idCategoria, String estado) {

        return eventoRepository.findAll((root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Filtro por estado
            if (estado != null && !estado.isBlank()) {
                predicates.add(cb.equal(root.get("estado"), estado.toUpperCase()));
            }

            // Filtro por nombre (búsqueda parcial e insensible a mayúsculas)
            if (nombre != null && !nombre.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
            }

            // Filtro por mes de inicio
            if (mes != null) {
                predicates.add(cb.equal(cb.function("month", Integer.class, root.get("fechaInicio")), mes));
            }

            // Filtro por categoría
            if (idCategoria != null) {
                predicates.add(cb.equal(root.get("categoria").get("idCategoria"), idCategoria));
            }

            // Ordenar por los más recientes primero
            query.orderBy(cb.desc(root.get("fechaCreacion")));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });
    }

    /**
     * Busca un evento por su ID único.
     */
    @Transactional(readOnly = true)
    public Evento consultarEventoPorId(Integer idEvento) {
        return eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }

    /**
     * Elimina un evento y todas sus dependencias (inscripciones, asistencias, organizadores).
     */
    @Transactional
    public void eliminarEvento(Integer idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        // Limpieza de tablas relacionadas para evitar errores de llave foránea
        entityManager.createNativeQuery("DELETE FROM evento_organizador WHERE id_evento = :idEvento")
                .setParameter("idEvento", idEvento)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM pre_checkin WHERE id_evento = :idEvento")
                .setParameter("idEvento", idEvento)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM asistencias WHERE id_evento = :idEvento")
                .setParameter("idEvento", idEvento)
                .executeUpdate();

        eventoRepository.delete(evento);
    }

    /**
     * Cambia el estado del evento a CANCELADO sin borrarlo.
     */
    @Transactional
    public void cancelarEvento(Integer idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        evento.setEstado("CANCELADO");
        eventoRepository.save(evento);
    }

    /**
     * Permite volver a poner un evento cancelado en estado PRÓXIMO.
     */
    @Transactional
    public void reactivarEvento(Integer idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (!"CANCELADO".equals(evento.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden reactivar eventos cancelados");
        }

        evento.setEstado("PRÓXIMO");
        eventoRepository.save(evento);
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A) SOBRE ESTE CÓDIGO:
     * 
     * 1. ¿Qué pasaría si el ID del creador enviado no es un Administrador?
     *    - Respuesta: Se lanzará una SecurityException ("Solo los administradores pueden crear eventos"),
     *      que será capturada en el controlador y devuelta como un error 403 Forbidden.
     * 
     * 2. ¿Qué pasaría si intento poner una fecha de fin anterior a la de inicio?
     *    - Respuesta: Se lanzará una IllegalArgumentException y el evento no se guardará.
     * 
     * 3. ¿Dónde está la lógica si piden agregar un nuevo campo a los eventos?
     *    - Respuesta: Tendrías que:
     *      1. Agregar el campo en la entidad [Evento.java].
     *      2. Agregarlo en el DTO [EventoCreateRequest.java].
     *      3. Actualizar el método 'crearEvento' aquí para asignar ese nuevo valor al modelo.
     * 
     * 4. ¿Por qué se usa 'createNativeQuery' para los organizadores?
     *    - Respuesta: Para manejar la tabla intermedia 'evento_organizador' de forma rápida y directa
     *      sin necesidad de mapear una entidad completa para la relación.
     */
}

    @Transactional
    public void actualizarEvento(Integer idEvento, EventoUpdateRequest request) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();
        boolean eventoYaOcurrio = evento.getFechaFin() != null && evento.getFechaFin().isBefore(ahora);

        LocalDateTime nuevaFechaInicio = request.getFechaInicio() != null ? request.getFechaInicio() : evento.getFechaInicio();
        LocalDateTime nuevaFechaFin = request.getFechaFin() != null ? request.getFechaFin() : evento.getFechaFin();
        String nuevoNombre = request.getNombre() != null ? request.getNombre() : evento.getNombre();

        if (request.getFechaInicio() != null || request.getFechaFin() != null) {
            if (eventoYaOcurrio) {
                throw new IllegalStateException("No se puede modificar la fecha y hora si el evento ya ocurrió");
            }
        }

        if (request.getNombre() != null && request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del evento no puede quedar vacío");
        }
        if (request.getDescripcion() != null && request.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción no puede quedar vacía");
        }
        if (request.getUbicacion() != null && request.getUbicacion().isBlank()) {
            throw new IllegalArgumentException("La ubicación no puede quedar vacía");
        }

        if (request.getCapacidadMaxima() != null && request.getCapacidadMaxima() <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser un número mayor a cero");
        }

        if (request.getTiempoCancelacionHoras() != null && request.getTiempoCancelacionHoras() <= 0) {
            throw new IllegalArgumentException("El tiempo de aceptación de cancelación debe ser mayor a cero");
        }

        if (request.getTiempoToleranciaMinutos() != null && request.getTiempoToleranciaMinutos() < 0) {
            throw new IllegalArgumentException("El tiempo de tolerancia debe ser un número mayor o igual a cero");
        }

        if (request.getBanner() != null && !request.getBanner().isBlank()) {
            if (!request.getBanner().startsWith("data:image/")) {
                throw new IllegalArgumentException("El banner debe ser una imagen válida");
            }
        }

        if (request.getFechaInicio() != null) {
            if (!nuevaFechaInicio.isAfter(ahora)) {
                throw new IllegalArgumentException("La fecha de inicio debe ser posterior a la fecha y hora actual");
            }
        }

        if (request.getFechaFin() != null || request.getFechaInicio() != null) {
            if (nuevaFechaFin == null || nuevaFechaInicio == null || !nuevaFechaFin.isAfter(nuevaFechaInicio)) {
                throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
            }
        }

        if (nuevaFechaInicio != null && nuevoNombre != null) {
            if (eventoRepository.existsByNombreAndFechaInicioAndIdEventoNot(nuevoNombre, nuevaFechaInicio, idEvento)) {
                throw new IllegalStateException("Ya existe un evento con ese nombre en ese horario");
            }
        }

        Integer nuevoTiempoCancelacionHoras = request.getTiempoCancelacionHoras() != null
                ? request.getTiempoCancelacionHoras()
                : evento.getTiempoCancelacionHoras();

        if (nuevoTiempoCancelacionHoras != null && nuevaFechaInicio != null && nuevaFechaInicio.isAfter(ahora)) {
            long horasDisp = Duration.between(ahora, nuevaFechaInicio).toHours();
            if (nuevoTiempoCancelacionHoras > horasDisp) {
                throw new IllegalArgumentException("El tiempo de aceptación de cancelación no puede ser mayor al tiempo disponible antes del evento");
            }
        }

        if (request.getNombre() != null) evento.setNombre(request.getNombre());
        if (request.getDescripcion() != null) evento.setDescripcion(request.getDescripcion());
        if (request.getUbicacion() != null) evento.setUbicacion(request.getUbicacion());
        if (request.getCapacidadMaxima() != null) evento.setCapacidadMaxima(request.getCapacidadMaxima());
        if (request.getTiempoCancelacionHoras() != null) evento.setTiempoCancelacionHoras(request.getTiempoCancelacionHoras());
        if (request.getTiempoToleranciaMinutos() != null) evento.setTiempoToleranciaMinutos(request.getTiempoToleranciaMinutos());
        if (request.getBanner() != null) evento.setBanner(request.getBanner());
        if (request.getFechaInicio() != null) evento.setFechaInicio(request.getFechaInicio());
        if (request.getFechaFin() != null) evento.setFechaFin(request.getFechaFin());

        if (request.getIdCategoria() != null) {
            Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
            evento.setCategoria(categoria);
        }

        eventoRepository.save(evento);
    }
}
