package com.eventnode.eventnodeapi.controllers;

import com.eventnode.eventnodeapi.services.CategoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * CRUD REST sobre {@link com.eventnode.eventnodeapi.models.Categoria}.
 * <p>Usa mapas JSON genéricos en escritura para flexibilidad mínima del cliente; la validación fina
 * ocurre en {@link CategoriaService} (nombre requerido, duplicados → 409).</p>
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(categoriaService.listar());
        } catch (Exception ex) {
            return error("Error al obtener categorías");
        }
    }

    /** Cuerpo: {@code {"nombre":"..."}}. 201 con id y nombre persistidos en mayúsculas. */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            String nombre = body.get("nombre") != null ? body.get("nombre").toString() : null;
            var categoria = categoriaService.crear(nombre);
            Map<String, Object> resp = new HashMap<>();
            resp.put("mensaje", "Categoría creada exitosamente");
            resp.put("idCategoria", categoria.getIdCategoria());
            resp.put("nombre", categoria.getNombre());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());
        } catch (IllegalStateException ex) {
            return conflict(ex.getMessage());
        } catch (Exception ex) {
            return error("Error al crear categoría");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        try {
            String nombre = body.get("nombre") != null ? body.get("nombre").toString() : null;
            var categoria = categoriaService.actualizar(id, nombre);
            Map<String, Object> resp = new HashMap<>();
            resp.put("mensaje", "Categoría actualizada exitosamente");
            resp.put("idCategoria", categoria.getIdCategoria());
            resp.put("nombre", categoria.getNombre());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());
        } catch (IllegalStateException ex) {
            return conflict(ex.getMessage());
        } catch (Exception ex) {
            return error("Error al actualizar categoría");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            categoriaService.eliminar(id);
            Map<String, String> resp = new HashMap<>();
            resp.put("mensaje", "Categoría eliminada exitosamente");
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());
        } catch (IllegalStateException ex) {
            return conflict(ex.getMessage());
        } catch (Exception ex) {
            return error("Error al eliminar categoría");
        }
    }

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", msg));
    }

    private ResponseEntity<Map<String, String>> conflict(String msg) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", msg));
    }

    private ResponseEntity<Map<String, String>> error(String msg) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("mensaje", msg));
    }
}
