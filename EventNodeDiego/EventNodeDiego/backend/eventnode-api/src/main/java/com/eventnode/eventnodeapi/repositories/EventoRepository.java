package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Categoria;
import com.eventnode.eventnodeapi.models.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

/** Eventos con Specification para filtros dinámicos y unicidad nombre+fecha inicio. */
public interface EventoRepository extends JpaRepository<Evento, Integer>, JpaSpecificationExecutor<Evento> {

    Optional<Evento> findByNombreAndFechaInicio(String nombre, LocalDateTime fechaInicio);

    boolean existsByNombreAndFechaInicioAndIdEventoNot(String nombre, LocalDateTime fechaInicio, Integer idEvento);

    long countByCategoria(Categoria categoria);
}

