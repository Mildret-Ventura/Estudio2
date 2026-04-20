package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.dtos.EventoCreateRequest;
import com.eventnode.eventnodeapi.dtos.EventoResponse;
import com.eventnode.eventnodeapi.dtos.EventoUpdateRequest;
import com.eventnode.eventnodeapi.models.Categoria;
import com.eventnode.eventnodeapi.models.Evento;
import com.eventnode.eventnodeapi.models.Organizador;
import com.eventnode.eventnodeapi.repositories.CategoriaRepository;
import com.eventnode.eventnodeapi.repositories.OrganizadorRepository;
import com.eventnode.eventnodeapi.repositories.PreCheckinRepository;
import com.eventnode.eventnodeapi.repositories.AsistenciaRepository;
import com.eventnode.eventnodeapi.services.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EventoController - Controlador de Eventos
 * 
 * ¿Qué es?: Es un controlador REST que gestiona todo el ciclo de vida de los eventos
 * (creación, consulta, edición, eliminación y cancelación), además de sus categorías y organizadores.
 * 
 * ¿Para qué sirve?: Proporciona los puntos de acceso para que el administrador gestione la 
 * cartelera de eventos y para que los estudiantes puedan ver los eventos disponibles.
 * 
 * ¿Cómo funciona?: Utiliza el servicio EventoService para la lógica compleja y repositorios 
 * directos para consultas simples. Devuelve información estructurada (DTOs) al frontend.
 */
@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    // Repositorios y servicios inyectados para interactuar con la base de datos
    private final EventoService eventoService;
    private final CategoriaRepository categoriaRepository;
    private final OrganizadorRepository organizadorRepository;
    private final PreCheckinRepository preCheckinRepository;
    private final AsistenciaRepository asistenciaRepository;

    public EventoController(EventoService eventoService,
                            CategoriaRepository categoriaRepository,
                            OrganizadorRepository organizadorRepository,
                            PreCheckinRepository preCheckinRepository,
                            AsistenciaRepository asistenciaRepository) {
        this.eventoService = eventoService;
        this.categoriaRepository = categoriaRepository;
        this.organizadorRepository = organizadorRepository;
        this.preCheckinRepository = preCheckinRepository;
        this.asistenciaRepository = asistenciaRepository;
    }

    /**
     * Crea un nuevo evento en el sistema.
     * Solo accesible por usuarios con rol de ADMINISTRADOR.
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearEvento(@Valid @RequestBody EventoCreateRequest request) {
        try {
            eventoService.crearEvento(request);
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", "Evento creado con éxito");
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (SecurityException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", ex.getMessage());
            return ResponseEntity.status(403).body(body);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("mensaje", ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    /**
     * Consulta eventos con filtros opcionales (nombre, mes, categoría, estado).
     * Usado tanto en la vista de administrador como de estudiante.
     */
    @GetMapping
    public ResponseEntity<?> consultarEventos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) String estado
    ) {
        List<Evento> eventos = eventoService.consultarEventosDisponibles(nombre, mes, categoriaId, estado);
        if (eventos.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        // Transforma los modelos de la base de datos a objetos de respuesta (DTOs)
        List<EventoResponse> response = eventos.stream()
                .map(e -> new EventoResponse(
                        e.getIdEvento(),
                        e.getBanner(),
                        e.getNombre(),
                        e.getUbicacion(),
                        e.getCapacidadMaxima(),
                        e.getTiempoCancelacionHoras(),
                        e.getFechaInicio(),
                        e.getFechaFin(),
                        e.getTiempoToleranciaMinutos(),
                        e.getDescripcion(),
                        e.getEstado(),
                        e.getCategoria() != null ? e.getCategoria().getIdCategoria() : null,
                        e.getCategoria() != null ? e.getCategoria().getNombre() : null,
                        preCheckinRepository.countByIdEventoAndEstado(e.getIdEvento(), "ACTIVO")
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la lista de categorías disponibles para clasificar eventos.
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<Map<String, Object>>> listarCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        List<Map<String, Object>> result = categorias.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idCategoria", c.getIdCategoria());
            map.put("nombre", c.getNombre());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Busca organizadores por nombre o lista todos si no hay filtro.
     */
    @GetMapping("/organizadores")
    public ResponseEntity<List<Map<String, Object>>> buscarOrganizadores(
            @RequestParam(required = false, defaultValue = "") String nombre) {
        List<Organizador> organizadores;
        if (nombre.isBlank()) {
            organizadores = organizadorRepository.findAll();
        } else {
            organizadores = organizadorRepository.findByNombreContainingIgnoreCase(nombre);
        }
        List<Map<String, Object>> result = organizadores.stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idOrganizador", o.getIdOrganizador());
            map.put("nombre", o.getNombre());
            map.put("correo", o.getCorreo());
            map.put("descripcion", o.getDescripcion());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Registra un nuevo organizador de eventos.
     */
    @PostMapping("/organizadores")
    public ResponseEntity<Map<String, Object>> crearOrganizador(@RequestBody Map<String, String> body) {
        String nombreOrg = body.get("nombre");
        if (nombreOrg == null || nombreOrg.isBlank()) {
            Map<String, Object> error = new HashMap<>();
            error.put("mensaje", "El nombre del organizador es obligatorio");
            return ResponseEntity.badRequest().body(error);
        }
        Organizador org = new Organizador();
        org.setNombre(nombreOrg.trim());
        org.setCorreo(body.get("correo"));
        org.setDescripcion(body.get("descripcion"));
        Organizador saved = organizadorRepository.save(org);

        Map<String, Object> result = new HashMap<>();
        result.put("idOrganizador", saved.getIdOrganizador());
        result.put("nombre", saved.getNombre());
        result.put("correo", saved.getCorreo());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Actualiza la información de un organizador existente.
     */
    @PutMapping("/organizadores/{id}")
    public ResponseEntity<Map<String, Object>> actualizarOrganizador(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        Organizador org = organizadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));
        if (body.containsKey("nombre") && body.get("nombre") != null && !body.get("nombre").isBlank()) {
            org.setNombre(body.get("nombre").trim());
        }
        if (body.containsKey("correo")) {
            org.setCorreo(body.get("correo"));
        }
        if (body.containsKey("descripcion")) {
            org.setDescripcion(body.get("descripcion"));
        }
        Organizador saved = organizadorRepository.save(org);
        Map<String, Object> result = new HashMap<>();
        result.put("idOrganizador", saved.getIdOrganizador());
        result.put("nombre", saved.getNombre());
        result.put("correo", saved.getCorreo());
        return ResponseEntity.ok(result);
    }

    /**
     * Elimina un organizador del sistema.
     */
    @DeleteMapping("/organizadores/{id}")
    public ResponseEntity<Map<String, String>> eliminarOrganizador(@PathVariable Integer id) {
        if (!organizadorRepository.existsById(id)) {
            throw new IllegalArgumentException("Organizador no encontrado");
        }
        organizadorRepository.deleteById(id);
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", "Organizador eliminado con exito");
        return ResponseEntity.ok(body);
    }

    /**
     * Obtiene todos los detalles de un evento específico, incluyendo conteo de inscritos y asistencias.
     */
    @GetMapping("/{idEvento}")
    public ResponseEntity<?> obtenerEventoDetalle(@PathVariable Integer idEvento) {
        Evento evento = eventoService.consultarEventoPorId(idEvento);

        Map<String, Object> response = new HashMap<>();
        response.put("idEvento", evento.getIdEvento());
        response.put("nombre", evento.getNombre());
        response.put("descripcion", evento.getDescripcion());
        response.put("ubicacion", evento.getUbicacion());
        response.put("capacidadMaxima", evento.getCapacidadMaxima());
        response.put("tiempoCancelacionHoras", evento.getTiempoCancelacionHoras());
        response.put("fechaInicio", evento.getFechaInicio());
        response.put("fechaFin", evento.getFechaFin());
        response.put("tiempoToleranciaMinutos", evento.getTiempoToleranciaMinutos());
        response.put("estado", evento.getEstado());
        response.put("banner", evento.getBanner());
        response.put("creadoPor", evento.getCreadoPor());
        response.put("fechaCreacion", evento.getFechaCreacion());

        if (evento.getCategoria() != null) {
            response.put("idCategoria", evento.getCategoria().getIdCategoria());
            response.put("categoriaNombre", evento.getCategoria().getNombre());
        }

        long inscritos = preCheckinRepository.countByIdEventoAndEstado(idEvento, "ACTIVO");
        response.put("inscritos", inscritos);

        long asistencias = asistenciaRepository.countByIdEvento(idEvento);
        response.put("asistencias", asistencias);

        List<Organizador> organizadores = organizadorRepository.findByEventoId(idEvento);
        List<Map<String, Object>> orgsList = organizadores.stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idOrganizador", o.getIdOrganizador());
            map.put("nombre", o.getNombre());
            map.put("correo", o.getCorreo());
            return map;
        }).collect(Collectors.toList());
        response.put("organizadores", orgsList);

        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza los datos de un evento existente.
     */
    @PutMapping("/{idEvento}")
    public ResponseEntity<Map<String, String>> actualizarEvento(@PathVariable Integer idEvento,
                                                               @RequestBody EventoUpdateRequest request) {
        eventoService.actualizarEvento(idEvento, request);
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", "Evento actualizado con éxito");
        return ResponseEntity.ok(body);
    }

    /**
     * Elimina físicamente un evento de la base de datos.
     */
    @DeleteMapping("/{idEvento}")
    public ResponseEntity<Map<String, String>> eliminarEvento(@PathVariable Integer idEvento) {
        eventoService.eliminarEvento(idEvento);
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", "Evento eliminado con éxito");
        return ResponseEntity.ok(body);
    }

    /**
     * Cambia el estado de un evento a CANCELADO.
     */
    @PostMapping("/{idEvento}/cancelar")
    public ResponseEntity<Map<String, String>> cancelarEvento(@PathVariable Integer idEvento) {
        eventoService.cancelarEvento(idEvento);
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", "Evento cancelado con éxito");
        return ResponseEntity.ok(body);
    }

    // --- MANEJADORES DE EXCEPCIONES ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getDefaultMessage())
                .orElse("Error de validación");
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", mensaje);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentos(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> manejarEstado(IllegalStateException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A) SOBRE ESTE CÓDIGO:
     * 
     * 1. ¿Qué pasaría si intento crear un evento con una categoría que no existe?
     *    - Respuesta: El 'eventoService' lanzará una excepción al no encontrar la categoría, 
     *      y el manejador de excepciones devolverá un error 400 con el mensaje correspondiente.
     * 
     * 2. ¿Qué pasaría si la capacidad máxima es menor a 1?
     *    - Respuesta: La validación @Valid en el DTO 'EventoCreateRequest' fallará y el método 
     *      'handleValidation' capturará el error antes de intentar guardarlo.
     * 
     * 3. ¿Dónde está la lógica si piden cambiar un botón de "Crear Evento"?
     *    - Respuesta: La lógica visual está en el componente [CrearEventoModal.jsx] del frontend. 
     *      Ese botón llama a la función 'createEvent' en [eventService.js], la cual finalmente 
     *      hace una petición POST a '/api/eventos/crear' que es manejada aquí.
     * 
     * 4. ¿Qué sucede con los inscritos si cancelo un evento?
     *    - Respuesta: La lógica está en 'eventoService.cancelarEvento'. Dependiendo de la 
     *      implementación, podría notificar a los usuarios o simplemente cambiar el estado.
     */
}
