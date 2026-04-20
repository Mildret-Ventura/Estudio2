package com.eventnode.eventnodeapi.schedulers;

import com.eventnode.eventnodeapi.models.Evento;
import com.eventnode.eventnodeapi.repositories.EventoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tarea en segundo plano que mantiene coherente el campo {@code eventos.estado} con la hora actual.
 * <p>No cancela eventos: solo refleja que el periodo académico del evento ya comenzó o ya terminó.
 * Los estados {@code CANCELADO} no se modifican aquí.</p>
 */
@Component
public class EventoScheduler {

    private final EventoRepository eventoRepository;

    public EventoScheduler(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    /**
     * Ejecuta cada 60 segundos ({@code fixedRate} = milisegundos entre inicios de ejecución).
     * <p>{@code @Transactional}: cada corrida puede hacer varios {@code save}; si falla uno, se revierte el lote.</p>
     * <p><strong>Nota de rendimiento:</strong> hace {@code findAll()} — aceptable para volúmenes académicos modestos;
     * con miles de eventos convendría consulta filtrada por rango de fechas.</p>
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void actualizarEstadosEventos() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Evento> eventos = eventoRepository.findAll();
        for (Evento evento : eventos) {
            String estado = evento.getEstado();

            // !fechaInicio.isAfter(ahora)  <=>  fechaInicio <= ahora  (el evento ya "empezó" o está en curso)
            if ("PRÓXIMO".equals(estado) && evento.getFechaInicio() != null
                    && !evento.getFechaInicio().isAfter(ahora)) {
                evento.setEstado("ACTIVO");
                eventoRepository.save(evento);
            } else if ("ACTIVO".equals(estado) && evento.getFechaFin() != null
                    && evento.getFechaFin().isBefore(ahora)) {
                // Strictly before: en el instante exacto de fin aún se considera ACTIVO hasta el tick siguiente
                evento.setEstado("FINALIZADO");
                eventoRepository.save(evento);
            }
        }
    }
}
