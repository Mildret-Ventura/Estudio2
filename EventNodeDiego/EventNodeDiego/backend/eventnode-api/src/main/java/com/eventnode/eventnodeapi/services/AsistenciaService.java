package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.models.Asistencia;
import com.eventnode.eventnodeapi.models.Evento;
import com.eventnode.eventnodeapi.models.PreCheckin;
import com.eventnode.eventnodeapi.models.Usuario;
import com.eventnode.eventnodeapi.models.Alumno;
import com.eventnode.eventnodeapi.repositories.AsistenciaRepository;
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
 * Registro físico de asistencia en evento. Depende de que el {@link EventoScheduler} haya pasado
 * el evento a estado ACTIVO en la hora de inicio; el check-in solo es válido en una ventana simétrica
 * alrededor de {@link Evento#getFechaInicio()} definida por {@link Evento#getTiempoToleranciaMinutos()}.
 */
@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final PreCheckinRepository preCheckinRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository,
                            PreCheckinRepository preCheckinRepository,
                            EventoRepository eventoRepository,
                            UsuarioRepository usuarioRepository,
                            AlumnoRepository alumnoRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.preCheckinRepository = preCheckinRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
    }

    /**
     * Crea una fila {@link Asistencia} en estado PENDIENTE (validación manual por staff vía {@link #actualizarEstado}).
     *
     * @param idUsuario quien asiste (debe estar preinscrito ACTIVO)
     * @param idEvento  evento en curso (estado ACTIVO)
     * @param metodo    texto libre almacenado en BD; en API suele ser QR o MANUAL según el cliente
     */
    @Transactional
    public void registrarAsistencia(Integer idUsuario, Integer idEvento, String metodo) {
        PreCheckin preCheckin = preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no está inscrito en este evento"));

        if (!"ACTIVO".equals(preCheckin.getEstado())) {
            throw new IllegalStateException("La inscripción no está activa");
        }

        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (!"ACTIVO".equals(evento.getEstado())) {
            throw new IllegalStateException("El evento no está activo");
        }

        if (asistenciaRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento).isPresent()) {
            throw new IllegalStateException("El usuario ya ha registrado asistencia");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTolerance = evento.getFechaInicio().minusMinutes(evento.getTiempoToleranciaMinutos());
        LocalDateTime endTolerance = evento.getFechaInicio().plusMinutes(evento.getTiempoToleranciaMinutos());

        if (now.isBefore(startTolerance) || now.isAfter(endTolerance)) {
            throw new IllegalStateException("No estás dentro del tiempo permitido para registrar asistencia");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setIdUsuario(idUsuario);
        asistencia.setIdEvento(idEvento);
        asistencia.setFechaCheckin(now);
        asistencia.setMetodo(metodo);
        asistencia.setEstado("PENDIENTE");

        asistenciaRepository.save(asistencia);
    }

    /**
     * Resuelve matrícula → {@code idUsuario} y delega en {@link #registrarAsistencia(Integer, Integer, String)}.
     */
    @Transactional
    public void registrarAsistenciaManual(String matricula, Integer idEvento, String metodo) {
        Alumno alumno = alumnoRepository.findByMatricula(matricula)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado con esa matrícula"));

        registrarAsistencia(alumno.getIdUsuario(), idEvento, metodo);
    }

    /** Panel admin: todas las asistencias del evento con nombre y correo resueltos. */
    public List<Map<String, Object>> listarAsistencias(Integer idEvento) {
        List<Asistencia> asistencias = asistenciaRepository.findByIdEvento(idEvento);

        return asistencias.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idAsistencia", a.getIdAsistencia());
            map.put("idUsuario", a.getIdUsuario());

            Usuario usuario = usuarioRepository.findById(a.getIdUsuario()).orElse(null);
            if (usuario != null) {
                String fullName = usuario.getNombre() + " " + usuario.getApellidoPaterno();
                if (usuario.getApellidoMaterno() != null && !usuario.getApellidoMaterno().isEmpty()) {
                    fullName += " " + usuario.getApellidoMaterno();
                }
                map.put("nombre", fullName);
                map.put("correo", usuario.getCorreo());

                Alumno alumno = alumnoRepository.findById(a.getIdUsuario()).orElse(null);
                if (alumno != null) {
                    map.put("cuatrimestre", alumno.getCuatrimestre());
                }
            }

            map.put("metodo", a.getMetodo());
            map.put("estado", a.getEstado());
            map.put("fechaCheckin", a.getFechaCheckin());

            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Flujo de validación de un check-in QR: el admin confirma o deja pendiente explícitamente.
     *
     * @param idAsistencia PK de {@link Asistencia}
     * @param estado       solo se aceptan los literales PENDIENTE o ASISTIDO
     */
    @Transactional
    public void actualizarEstado(Integer idAsistencia, String estado) {
        Asistencia asistencia = asistenciaRepository.findById(idAsistencia)
                .orElseThrow(() -> new IllegalArgumentException("Asistencia no encontrada"));

        if (!"PENDIENTE".equals(estado) && !"ASISTIDO".equals(estado)) {
            throw new IllegalArgumentException("Estado inválido. Debe ser PENDIENTE o ASISTIDO");
        }

        asistencia.setEstado(estado);
        asistenciaRepository.save(asistencia);
    }

    public long contarAsistencias(Integer idEvento) {
        return asistenciaRepository.countByIdEvento(idEvento);
    }
}
