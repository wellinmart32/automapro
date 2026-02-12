package com.automapro.backend.controller;

import com.automapro.backend.dto.AplicacionDTO;
import com.automapro.backend.service.AplicacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de aplicaciones (solo ADMIN)
 */
@RestController
@RequestMapping("/api/admin/aplicaciones")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
@PreAuthorize("hasRole('ADMIN')")
public class AplicacionController {

    @Autowired
    private AplicacionService aplicacionService;

    /**
     * Listar todas las aplicaciones
     * GET /api/admin/aplicaciones
     */
    @GetMapping
    public ResponseEntity<List<AplicacionDTO>> listarTodas() {
        return ResponseEntity.ok(aplicacionService.listarTodas());
    }

    /**
     * Listar solo aplicaciones activas
     * GET /api/admin/aplicaciones/activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<AplicacionDTO>> listarActivas() {
        return ResponseEntity.ok(aplicacionService.listarActivas());
    }

    /**
     * Obtener una aplicación por ID
     * GET /api/admin/aplicaciones/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            AplicacionDTO aplicacion = aplicacionService.obtenerPorId(id);
            return ResponseEntity.ok(aplicacion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Buscar aplicaciones por nombre
     * GET /api/admin/aplicaciones/buscar?nombre=XXX
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<AplicacionDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(aplicacionService.buscarPorNombre(nombre));
    }

    /**
     * Crear una nueva aplicación
     * POST /api/admin/aplicaciones
     */
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody AplicacionDTO aplicacionDTO) {
        try {
            AplicacionDTO creada = aplicacionService.crear(aplicacionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear aplicación: " + e.getMessage());
        }
    }

    /**
     * Actualizar una aplicación existente
     * PUT /api/admin/aplicaciones/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody AplicacionDTO aplicacionDTO) {
        try {
            AplicacionDTO actualizada = aplicacionService.actualizar(id, aplicacionDTO);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar aplicación: " + e.getMessage());
        }
    }

    /**
     * Eliminar una aplicación (borrado lógico)
     * DELETE /api/admin/aplicaciones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            aplicacionService.eliminar(id);
            return ResponseEntity.ok("Aplicación eliminada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar aplicación: " + e.getMessage());
        }
    }
}