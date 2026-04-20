package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistencia de {@link com.eventnode.eventnodeapi.models.Alumno}; PK = id de usuario. */
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    Optional<Alumno> findByMatricula(String matricula);
}

