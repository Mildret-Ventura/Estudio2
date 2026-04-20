package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Asistencias por evento y par usuario-evento. */
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    List<Asistencia> findByIdEvento(Integer idEvento);

    Optional<Asistencia> findByIdUsuarioAndIdEvento(Integer idUsuario, Integer idEvento);

    long countByIdEvento(Integer idEvento);
}
