package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Organizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Organizadores y consulta nativa por evento (tabla puente {@code evento_organizador}). */
public interface OrganizadorRepository extends JpaRepository<Organizador, Integer> {

    List<Organizador> findByNombreContainingIgnoreCase(String nombre);

    @Query(value = "SELECT o.* FROM organizadores o JOIN evento_organizador eo ON o.id_organizador = eo.id_organizador WHERE eo.id_evento = :idEvento", nativeQuery = true)
    List<Organizador> findByEventoId(@Param("idEvento") Integer idEvento);
}

