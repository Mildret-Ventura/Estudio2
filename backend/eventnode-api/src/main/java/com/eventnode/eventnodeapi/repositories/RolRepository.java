package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);
}

