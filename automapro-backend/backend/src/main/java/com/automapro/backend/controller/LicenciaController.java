package com.automapro.backend.controller;

import com.automapro.backend.dto.LicenciaDTO;
import com.automapro.backend.service.LicenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de licencias (solo ADMIN)
 */
@RestController
@RequestMapping("/api/admin/licencias")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
@PreAuthorize("hasRole('ADMIN')")
public class LicenciaController {

    @Autowired
    private LicenciaService licenciaService;

    /**
     * Listar todas las licencias
     * GET /api/admin/licencias
     */
    @GetMapping
    public ResponseEntity<List<LicenciaDTO>> listarTodas() {
        return ResponseEntity.ok(licenciaService.listarTodas());
    }

    /**
     * Listar licencias de un usuario específico
     * GET /api/admin/licencias/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<LicenciaDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(licenciaService.listarPorUsuario(usuarioId));
    }

    /**
     * Obtener una licencia por ID
     * GET /api/admin/licencias/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            LicenciaDTO licencia = licenciaService.obtenerPorId(id);
            return ResponseEntity.ok(licencia);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Crear una nueva licencia
     * POST /api/admin/licencias
     */
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody LicenciaDTO licenciaDTO) {
        try {
            LicenciaDTO creada = licenciaService.crear(licenciaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear licencia: " + e.getMessage());
        }
    }

    /**
     * Actualizar una licencia existente
     * PUT /api/admin/licencias/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody LicenciaDTO licenciaDTO) {
        try {
            LicenciaDTO actualizada = licenciaService.actualizar(id, licenciaDTO);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar licencia: " + e.getMessage());
        }
    }

    /**
     * Eliminar una licencia (borrado lógico)
     * DELETE /api/admin/licencias/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            licenciaService.eliminar(id);
            return ResponseEntity.ok("Licencia eliminada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar licencia: " + e.getMessage());
        }
    }
}