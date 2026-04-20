package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.models.Evento;
import com.eventnode.eventnodeapi.models.PreCheckin;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.models.Alumno;
import com.eventnode.eventnodeapi.repositories.PreCheckinRepository;
import com.eventnode.eventnodeapi.repositories.EventoRepository;
import com.eventnode.eventnodeapi.repositories.UsuarioRepository;
import com.eventnode.eventnodeapi.repositories.AlumnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * PreCheckinService - Servicio de Gestión de Inscripciones
 * 
 * ¿Qué es?: Es el componente encargado de procesar el registro previo de los 
 * alumnos a los eventos, manejando el cupo y los plazos de cancelación.
 * 
 * ¿Para qué sirve?: Asegura que un evento no exceda su capacidad máxima y que los 
 * alumnos se comprometan a asistir, permitiendo cancelaciones solo bajo reglas justas.
 * 
 * ¿Cómo funciona?: 
 * - Valida que el usuario sea un Alumno activo.
 * - Verifica la capacidad máxima del evento antes de confirmar un lugar.
 * - Permite reactivar inscripciones previamente canceladas.
 * - Aplica la regla de "Tiempo Límite de Cancelación" configurada en cada evento.
 * - Genera listas detalladas de inscritos para el administrador.
 */
@Service
public class PreCheckinService {

    private final PreCheckinRepository preCheckinRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;

    public PreCheckinService(PreCheckinRepository preCheckinRepository,
                           EventoRepository eventoRepository,
                           UsuarioRepository usuarioRepository,
                           AlumnoRepository alumnoRepository) {
        this.preCheckinRepository = preCheckinRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
    }

    /**
     * Proceso de inscripción a un evento.
     */
    @Transactional
    public void inscribirse(Integer idUsuario, Integer idEvento) {
        // 1. Validar existencia del usuario y que sea un ALUMNO
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Alumno alumno = alumnoRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no es un alumno"));

        // 2. Validar que el evento esté disponible
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (!"ACTIVO".equals(evento.getEstado()) && !"PRÓXIMO".equals(evento.getEstado())) {
            throw new IllegalStateException("El evento no está disponible para inscripción");
        }

        // 3. Validar que el evento no haya comenzado ya
        if (LocalDateTime.now().isAfter(evento.getFechaInicio())) {
            throw new IllegalStateException("El pre-check-in ya no está disponible, el evento ya ha iniciado");
        }

        // 4. Validar disponibilidad de cupo
        long countInscritos = preCheckinRepository.countByIdEventoAndEstado(idEvento, "ACTIVO");
        if (countInscritos >= evento.getCapacidadMaxima()) {
            throw new IllegalStateException("El evento está lleno");
        }

        // 5. Manejar inscripciones existentes (evitar duplicados o reactivar canceladas)
        var existingOpt = preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento);
        if (existingOpt.isPresent()) {
            PreCheckin existing = existingOpt.get();
            if ("ACTIVO".equals(existing.getEstado())) {
                throw new IllegalStateException("Ya cuentas con un lugar en este evento");
            }
            // Reactivación de un lugar previamente cancelado
            existing.setEstado("ACTIVO");
            existing.setFechaRegistro(LocalDateTime.now());
            preCheckinRepository.save(existing);
            return;
        }

        // 6. Crear nuevo registro de PreCheckin
        PreCheckin preCheckin = new PreCheckin();
        preCheckin.setIdUsuario(idUsuario);
        preCheckin.setIdEvento(idEvento);
        preCheckin.setFechaRegistro(LocalDateTime.now());
        preCheckin.setEstado("ACTIVO");

        preCheckinRepository.save(preCheckin);
    }

    /**
     * Cancela una inscripción activa respetando el tiempo límite.
     */
    @Transactional
    public void cancelarInscripcion(Integer idUsuario, Integer idEvento) {
        PreCheckin preCheckin = preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la inscripción"));

        if (!"ACTIVO".equals(preCheckin.getEstado())) {
            throw new IllegalStateException("La inscripción no está activa");
        }

        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        // Regla de Negocio: Validar tiempo de antelación para cancelar
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minCancelTime = evento.getFechaInicio().minusHours(evento.getTiempoCancelacionHoras());

        if (now.isAfter(minCancelTime)) {
            throw new IllegalStateException("Ya no es posible cancelar la inscripción. El tiempo límite ha expirado");
        }

        preCheckin.setEstado("CANCELADO");
        preCheckinRepository.save(preCheckin);
    }

    /**
     * Obtiene la lista de inscritos activos para un evento.
     */
    public List<Map<String, Object>> listarInscritos(Integer idEvento) {
        List<PreCheckin> preCheckins = preCheckinRepository.findByIdEvento(idEvento);
        List<PreCheckin> activosOnly = preCheckins.stream()
                .filter(pc -> "ACTIVO".equals(pc.getEstado()))
                .collect(Collectors.toList());

        return activosOnly.stream().map(pc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idPrecheckin", pc.getIdPrecheckin());
            map.put("idUsuario", pc.getIdUsuario());

            Usuario usuario = usuarioRepository.findById(pc.getIdUsuario()).orElse(null);
            if (usuario != null) {
                String fullName = usuario.getNombre() + " " + usuario.getApellidoPaterno();
                if (usuario.getApellidoMaterno() != null && !usuario.getApellidoMaterno().isEmpty()) {
                    fullName += " " + usuario.getApellidoMaterno();
                }
                map.put("nombre", fullName);
                map.put("correo", usuario.getCorreo());

                Alumno alumno = alumnoRepository.findById(pc.getIdUsuario()).orElse(null);
                if (alumno != null) {
                    map.put("matricula", alumno.getMatricula());
                }
            }

            map.put("fechaRegistro", pc.getFechaRegistro());
            map.put("estado", pc.getEstado());

            return map;
        }).collect(Collectors.toList());
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si un evento tiene capacidad para 50 y ya hay 50 inscritos?
     *    - Respuesta: El sistema lanzará una IllegalStateException ("El evento está lleno") 
     *      y no permitirá que el alumno número 51 se inscriba.
     * 
     * 2. ¿Puedo volver a inscribirme si cancelé por error?
     *    - Respuesta: Sí, siempre y cuando siga habiendo cupo y no haya pasado el 
     *      tiempo de inicio del evento. El sistema reactivará tu registro anterior.
     * 
     * 3. ¿Dónde está el botón de "Cancelar Inscripción"?
     *    - Respuesta: El alumno lo puede ver en su panel de "Mis Eventos" [StudentMyEvents.jsx]. 
     *      Ese botón llama a 'cancelarInscripcion' en este servicio.
     * 
     * 4. ¿Qué sucede con mi lugar si cancelo?
     *    - Respuesta: Tu estado cambia a 'CANCELADO' y el cupo se libera inmediatamente 
     *      para que otro alumno pueda inscribirse.
     */
}

    public List<Map<String, Object>> listarEventosInscritos(Integer idUsuario) {
        List<PreCheckin> preCheckins = preCheckinRepository.findByIdUsuario(idUsuario);

        return preCheckins.stream().map(pc -> {
            Map<String, Object> map = new HashMap<>();

            Evento evento = eventoRepository.findById(pc.getIdEvento()).orElse(null);
            if (evento != null) {
                map.put("idEvento", evento.getIdEvento());
                map.put("nombre", evento.getNombre());
                map.put("ubicacion", evento.getUbicacion());
                map.put("fechaInicio", evento.getFechaInicio());
                map.put("fechaFin", evento.getFechaFin());
                map.put("estado", evento.getEstado());
                map.put("banner", evento.getBanner());
                map.put("tiempoToleranciaMinutos", evento.getTiempoToleranciaMinutos());

                map.put("categoriaNombre", evento.getCategoria() != null ? evento.getCategoria().getNombre() : "Sin categoría");
            }

            map.put("inscripcionEstado", pc.getEstado());

            return map;
        }).collect(Collectors.toList());
    }

    public long contarInscritos(Integer idEvento) {
        return preCheckinRepository.countByIdEventoAndEstado(idEvento, "ACTIVO");
    }
}
