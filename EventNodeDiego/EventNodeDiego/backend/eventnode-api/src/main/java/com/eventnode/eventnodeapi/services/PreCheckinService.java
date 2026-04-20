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
import java.util.stream.Collectors;

/**
 * Reglas de negocio de la tabla {@code pre_checkin}: cupo del evento, ventana temporal para inscribirse
 * y para cancelar según {@link Evento#getTiempoCancelacionHoras()}.
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
     * Inscribe a un alumno en un evento o reactiva una fila previamente {@code CANCELADO}.
     *
     * @param idUsuario PK del usuario (debe existir fila en {@code alumnos} con la misma PK)
     * @param idEvento  evento destino
     * @throws IllegalArgumentException usuario inexistente, no alumno, evento inexistente
     * @throws IllegalStateException    evento no disponible, ya iniciado, lleno, o inscripción ya ACTIVO
     */
    @Transactional
    public void inscribirse(Integer idUsuario, Integer idEvento) {
        usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        alumnoRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no es un alumno"));

        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (!"ACTIVO".equals(evento.getEstado()) && !"PRÓXIMO".equals(evento.getEstado())) {
            throw new IllegalStateException("El evento no está disponible para inscripción");
        }

        if (LocalDateTime.now().isAfter(evento.getFechaInicio())) {
            throw new IllegalStateException("El pre-check-in ya no está disponible, el evento ya ha iniciado");
        }

        long countInscritos = preCheckinRepository.countByIdEventoAndEstado(idEvento, "ACTIVO");
        if (countInscritos >= evento.getCapacidadMaxima()) {
            throw new IllegalStateException("El evento está lleno");
        }

        var existingOpt = preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento);
        if (existingOpt.isPresent()) {
            PreCheckin existing = existingOpt.get();
            if ("ACTIVO".equals(existing.getEstado())) {
                throw new IllegalStateException("Ya cuentas con un lugar en este evento");
            }
            existing.setEstado("ACTIVO");
            existing.setFechaRegistro(LocalDateTime.now());
            preCheckinRepository.save(existing);
            return;
        }

        PreCheckin preCheckin = new PreCheckin();
        preCheckin.setIdUsuario(idUsuario);
        preCheckin.setIdEvento(idEvento);
        preCheckin.setFechaRegistro(LocalDateTime.now());
        preCheckin.setEstado("ACTIVO");

        preCheckinRepository.save(preCheckin);
    }

    /**
     * Marca la inscripción como CANCELADO si aún no se alcanzó la fecha límite:
     * {@code ahora <= fechaInicio - tiempoCancelacionHoras}.
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

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minCancelTime = evento.getFechaInicio().minusHours(evento.getTiempoCancelacionHoras());

        if (now.isAfter(minCancelTime)) {
            throw new IllegalStateException("Ya no es posible cancelar la inscripción. El tiempo límite ha expirado");
        }

        preCheckin.setEstado("CANCELADO");
        preCheckinRepository.save(preCheckin);
    }

    /**
     * Listado administrativo: solo filas de pre-checkin en estado ACTIVO para el evento dado.
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

    /**
     * Vista alumno: todos sus pre-checkins (cualquier estado) con datos denormalizados del evento.
     */
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

    /** Cuenta solo inscripciones ACTIVO (ocupan cupo). */
    public long contarInscritos(Integer idEvento) {
        return preCheckinRepository.countByIdEventoAndEstado(idEvento, "ACTIVO");
    }
}
