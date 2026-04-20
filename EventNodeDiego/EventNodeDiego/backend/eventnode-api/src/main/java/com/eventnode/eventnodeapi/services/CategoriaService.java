package com.eventnode.eventnodeapi.services;

import com.eventnode.eventnodeapi.models.Categoria;
import com.eventnode.eventnodeapi.repositories.CategoriaRepository;
import com.eventnode.eventnodeapi.repositories.EventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CRUD de {@link Categoria}. Los nombres se normalizan a MAYÚSCULAS para consistencia en listados y búsquedas.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EventoRepository eventoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, EventoRepository eventoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.eventoRepository = eventoRepository;
    }

    /**
     * @return lista de mapas con {@code idCategoria}, {@code nombre} y {@code totalEventos} (conteo por FK en eventos)
     */
    public List<Map<String, Object>> listar() {
        return categoriaRepository.findAll().stream().map(c -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("idCategoria", c.getIdCategoria());
            map.put("nombre", c.getNombre());
            long totalEventos = eventoRepository.countByCategoria(c);
            map.put("totalEventos", totalEventos);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * @param nombre texto no vacío; se recorta y compara duplicados sin sensibilidad a mayúsculas
     */
    @Transactional
    public Categoria crear(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre de la categoría es requerido");

        if (categoriaRepository.findByNombreIgnoreCase(nombre.trim()).isPresent())
            throw new IllegalStateException("Ya existe una categoría con ese nombre");

        Categoria c = new Categoria();
        c.setNombre(nombre.trim().toUpperCase());
        return categoriaRepository.save(c);
    }

    /**
     * @param idCategoria PK
     * @param nombre      nuevo nombre (mismas reglas que {@link #crear(String)})
     */
    @Transactional
    public Categoria actualizar(Integer idCategoria, String nombre) {
        Categoria c = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre es requerido");

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaIsNot(nombre.trim(), idCategoria))
            throw new IllegalStateException("Ya existe una categoría con ese nombre");

        c.setNombre(nombre.trim().toUpperCase());
        return categoriaRepository.save(c);
    }

    /**
     * Borrado solo si {@code countByCategoria == 0} para no dejar eventos huérfanos de categoría.
     */
    @Transactional
    public void eliminar(Integer idCategoria) {
        Categoria c = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        long eventos = eventoRepository.countByCategoria(c);
        if (eventos > 0)
            throw new IllegalStateException("No se puede eliminar: la categoría tiene " + eventos + " evento(s) asociado(s)");

        categoriaRepository.delete(c);
    }
}
