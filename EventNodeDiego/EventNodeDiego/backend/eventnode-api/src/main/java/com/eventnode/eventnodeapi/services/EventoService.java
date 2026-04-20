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
 * Casos de uso de {@link Evento}: alta con validaciones de negocio, búsqueda con JPA Criteria,
 * borrado en cascada manual (native SQL) sobre tablas relacionadas sin FK declarada en JPA para la puente,
 * y actualización parcial desde {@link com.eventnode.eventnodeapi.dtos.EventoUpdateRequest}.
 * <p>La relación evento↔organizador es N:M mediante {@code evento_organizador}; no hay entidad JPA dedicada,
 * por eso se usa {@link EntityManager#createNativeQuery}.</p>
 */
@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;
    @SuppressWarnings("unused")
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
     * Persiste un evento en estado PRÓXIMO y opcionalmente enlaza organizadores en la tabla puente.
     *
     * @param request DTO validado en capa web; {@code idCreador} debe ser ADMINISTRADOR o SUPERADMIN
     * @throws SecurityException        rol del creador no autorizado
     * @throws IllegalStateException    duplicado nombre+misma fechaInicio
     * @throws IllegalArgumentException validaciones de fechas, capacidad, banner, categoría, ventana de cancelación
     */
    @Transactional
    public void crearEvento(EventoCreateRequest request) {

        Usuario creador = usuarioRepository.findById(request.getIdCreador())
                .orElseThrow(() -> new IllegalArgumentException("Usuario creador no encontrado"));

        String rolCreador = creador.getRol() != null ? creador.getRol().getNombre() : null;
        if (!"ADMINISTRADOR".equals(rolCreador) && !"SUPERADMIN".equals(rolCreador)) {
            throw new SecurityException("Solo los administradores pueden crear eventos");
        }

        if (eventoRepository.findByNombreAndFechaInicio(request.getNombre(), request.getFechaInicio()).isPresent()) {
            throw new IllegalStateException("Ya existe un evento con ese nombre en ese horario");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (!request.getFechaInicio().isAfter(ahora)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser posterior a la fecha y hora actual");
        }
        if (!request.getFechaFin().isAfter(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        if (request.getCapacidadMaxima() == null || request.getCapacidadMaxima() <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser un número mayor a cero");
        }

        if (request.getTiempoCancelacionHoras() == null || request.getTiempoCancelacionHoras() <= 0) {
            throw new IllegalArgumentException("El tiempo de aceptación de cancelación debe ser mayor a cero");
        }
        long horasDisponibles = Duration.between(ahora, request.getFechaInicio()).toHours();
        if (request.getTiempoCancelacionHoras() > horasDisponibles) {
            throw new IllegalArgumentException("El tiempo de aceptación de cancelación no puede ser mayor al tiempo disponible antes del evento");
        }

        if (request.getTiempoToleranciaMinutos() == null || request.getTiempoToleranciaMinutos() < 0) {
            throw new IllegalArgumentException("El tiempo de tolerancia debe ser un número mayor o igual a cero");
        }

        String banner = request.getBanner();
        if (banner != null && !banner.isBlank()) {
            if (!banner.startsWith("data:image/")) {
                throw new IllegalArgumentException("El banner debe ser una imagen válida (PNG, JPG, JPEG)");
            }
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

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
     * Búsqueda dinámica: cada parámetro no nulo/vacío añade un predicado AND. Sin filtros devuelve todo ordenado
     * por {@code fechaCreacion} descendente.
     *
     * @param nombre      subcadena insensible a mayúsculas en nombre del evento
     * @param mes         1–12: filtro por función SQL MONTH(fecha_inicio)
     * @param idCategoria FK categoría
     * @param estado      se compara en MAYÚSCULAS con el valor persistido (PRÓXIMO, ACTIVO, …)
     */
    @Transactional(readOnly = true)
    public List<Evento> consultarEventosDisponibles(String nombre, Integer mes, Integer idCategoria, String estado) {

        return eventoRepository.findAll((root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (estado != null && !estado.isBlank()) {
                predicates.add(cb.equal(root.get("estado"), estado.toUpperCase()));
            }

            if (nombre != null && !nombre.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
            }

            if (mes != null) {
                predicates.add(cb.equal(cb.function("month", Integer.class, root.get("fechaInicio")), mes));
            }

            if (idCategoria != null) {
                predicates.add(cb.equal(root.get("categoria").get("idCategoria"), idCategoria));
            }

            query.orderBy(cb.desc(root.get("fechaCreacion")));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });
    }

    @Transactional(readOnly = true)
    public Evento consultarEventoPorId(Integer idEvento) {
        return eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }

    /**
     * Borrado físico: primero filas dependientes que podrían violar integridad referencial en MySQL,
     * luego la fila en {@code eventos}. No borra diplomas ni organizadores base, solo vínculos y asistencias/preinscripciones del evento.
     */
    @Transactional
    public void eliminarEvento(Integer idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

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

    /** Marca estado CANCELADO sin borrar filas (inscripciones históricas pueden seguir existiendo). */
    @Transactional
    public void cancelarEvento(Integer idEvento) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        evento.setEstado("CANCELADO");
        eventoRepository.save(evento);
    }

    /** Solo desde CANCELADO vuelve a PRÓXIMO (el scheduler seguirá el flujo normal después). */
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

    /**
     * PATCH semántico: solo se sobrescriben campos no nulos en el request. Revalida coherencia de fechas
     * y unicidad nombre+fechaInicio cuando estos cambian.
     */
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
