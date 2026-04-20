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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * AsistenciaService - Servicio de Registro de Entrada
 * 
 * ¿Qué es?: Es el componente que contiene las reglas críticas para permitir o 
 * denegar el registro de asistencia de un alumno a un evento.
 * 
 * ¿Para qué sirve?: Asegura que solo los alumnos inscritos puedan entrar, que lo 
 * hagan dentro del horario permitido y que no se dupliquen los registros.
 * 
 * ¿Cómo funciona?: 
 * - Valida la inscripción (PreCheckin) y el estado del evento.
 * - Calcula la "ventana de tiempo" permitida usando el tiempo de tolerancia del evento.
 * - Soporta tanto el registro directo (ID de usuario) como el manual (Matrícula).
 * - Transforma los datos crudos de asistencia en una lista legible con nombres de alumnos.
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
     * Registra la asistencia validando múltiples reglas de negocio.
     */
    @Transactional
    public void registrarAsistencia(Integer idUsuario, Integer idEvento, String metodo) {
        // 1. Validar que el usuario esté inscrito y activo
        PreCheckin preCheckin = preCheckinRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no está inscrito en este evento"));

        if (!"ACTIVO".equals(preCheckin.getEstado())) {
            throw new IllegalStateException("La inscripción no está activa");
        }

        // 2. Validar que el evento esté activo
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (!"ACTIVO".equals(evento.getEstado())) {
            throw new IllegalStateException("El evento no está activo");
        }

        // 3. Evitar duplicados
        if (asistenciaRepository.findByIdUsuarioAndIdEvento(idUsuario, idEvento).isPresent()) {
            throw new IllegalStateException("El usuario ya ha registrado asistencia");
        }

        // 4. Validar ventana de tiempo (Tolerancia)
        // Ejemplo: Si el evento inicia a las 10:00 y hay 15 min de tolerancia, 
        // se puede registrar de 09:45 a 10:15.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTolerance = evento.getFechaInicio().minusMinutes(evento.getTiempoToleranciaMinutos());
        LocalDateTime endTolerance = evento.getFechaInicio().plusMinutes(evento.getTiempoToleranciaMinutos());

        if (now.isBefore(startTolerance) || now.isAfter(endTolerance)) {
            throw new IllegalStateException("No estás dentro del tiempo permitido para registrar asistencia");
        }

        // 5. Crear registro de Asistencia
        Asistencia asistencia = new Asistencia();
        asistencia.setIdUsuario(idUsuario);
        asistencia.setIdEvento(idEvento);
        asistencia.setFechaCheckin(now);
        asistencia.setMetodo(metodo);
        asistencia.setEstado("PENDIENTE"); // Inicia como pendiente hasta ser validada

        asistenciaRepository.save(asistencia);
    }

    /**
     * Facilita el registro manual buscando al usuario por su matrícula.
     */
    @Transactional
    public void registrarAsistenciaManual(String matricula, Integer idEvento, String metodo) {
        Alumno alumno = alumnoRepository.findByMatricula(matricula)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado con esa matrícula"));

        registrarAsistencia(alumno.getIdUsuario(), idEvento, metodo);
    }

    /**
     * Genera una lista enriquecida de asistentes con nombres, correos y datos académicos.
     */
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
     * Cambia el estado de una asistencia.
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

    /**
     * Conteo rápido de asistentes confirmados.
     */
    public long contarAsistencias(Integer idEvento) {
        return asistenciaRepository.countByIdEvento(idEvento);
    }

    /*
     * SECCIÓN DE PREGUNTAS FRECUENTES (Q&A):
     * 
     * 1. ¿Qué pasaría si el reloj del servidor está mal configurado?
     *    - Respuesta: La validación de la ventana de tolerancia fallaría, impidiendo 
     *      que los alumnos registren su asistencia correctamente.
     * 
     * 2. ¿Cómo funciona la tolerancia exactamente?
     *    - Respuesta: Se resta y suma el valor de 'tiempoToleranciaMinutos' a la 
     *      fecha de inicio del evento para crear un rango de tiempo válido.
     * 
     * 3. ¿Dónde está la lógica de los botones para "Confirmar Asistencia"?
     *    - Respuesta: En la página [AdminCheckIn.jsx] del frontend. El admin ve la lista 
     *      de los que hicieron check-in y pulsa un botón que llama a 'actualizarEstado' aquí.
     * 
     * 4. ¿Qué pasa si elimino a un alumno que tenía asistencias?
     *    - Respuesta: Depende de las restricciones de la DB, pero generalmente el 
     *      [UsuarioService] debe limpiar estos registros primero para evitar errores foráneos.
     */
}
