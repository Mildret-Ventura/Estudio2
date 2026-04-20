package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Categorías de evento con consultas por nombre (ignorando mayúsculas). */
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdCategoriaIsNot(String nombre, Integer idCategoria);
}
