package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.PreCheckin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreCheckinRepository extends JpaRepository<PreCheckin, Integer> {

    List<PreCheckin> findByIdEvento(Integer idEvento);

    Optional<PreCheckin> findByIdUsuarioAndIdEvento(Integer idUsuario, Integer idEvento);

    List<PreCheckin> findByIdUsuario(Integer idUsuario);

    long countByIdEventoAndEstado(Integer idEvento, String estado);
}
