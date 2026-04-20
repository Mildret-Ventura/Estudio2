package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Diploma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiplomaRepository extends JpaRepository<Diploma, Integer> {

    Optional<Diploma> findByIdEvento(Integer idEvento);

    List<Diploma> findByEstado(String estado);
}
