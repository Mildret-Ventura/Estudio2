package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistencia de {@link com.eventnode.eventnodeapi.models.Administrador}. */
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
}
