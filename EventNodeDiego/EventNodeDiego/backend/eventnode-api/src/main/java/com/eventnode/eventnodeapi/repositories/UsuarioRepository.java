package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistencia de {@link com.eventnode.eventnodeapi.models.Usuario}; búsqueda por correo. */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);
}

