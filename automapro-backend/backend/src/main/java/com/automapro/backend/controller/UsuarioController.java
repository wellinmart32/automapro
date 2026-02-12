package com.automapro.backend.controller;

import com.automapro.backend.dto.UsuarioDTO;
import com.automapro.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de usuarios (solo ADMIN)
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Listar todos los usuarios
     * GET /api/admin/usuarios
     */
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Obtener un usuario por ID
     * GET /api/admin/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            UsuarioDTO usuario = usuarioService.obtenerPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Crear un nuevo usuario
     * POST /api/admin/usuarios
     */
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Map<String, Object> request) {
        try {
            UsuarioDTO usuarioDTO = new UsuarioDTO();
            usuarioDTO.setNombre((String) request.get("nombre"));
            usuarioDTO.setEmail((String) request.get("email"));
            usuarioDTO.setRol((String) request.get("rol"));
            usuarioDTO.setActivo((Boolean) request.getOrDefault("activo", true));

            String password = (String) request.get("password");

            UsuarioDTO creado = usuarioService.crear(usuarioDTO, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear usuario: " + e.getMessage());
        }
    }

    /**
     * Actualizar un usuario existente
     * PUT /api/admin/usuarios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDTO usuarioDTO) {
        try {
            UsuarioDTO actualizado = usuarioService.actualizar(id, usuarioDTO);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    /**
     * Eliminar un usuario (borrado lógico)
     * DELETE /api/admin/usuarios/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            usuarioService.eliminar(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar usuario: " + e.getMessage());
        }
    }

    /**
     * Cambiar contraseña de un usuario
     * PUT /api/admin/usuarios/{id}/password
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String nuevoPassword = request.get("password");
            usuarioService.cambiarPassword(id, nuevoPassword);
            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cambiar contraseña: " + e.getMessage());
        }
    }
}