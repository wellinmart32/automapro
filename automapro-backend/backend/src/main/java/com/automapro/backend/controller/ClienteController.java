package com.automapro.backend.controller;

import com.automapro.backend.dto.LicenciaDTO;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.repository.UsuarioRepository;
import com.automapro.backend.service.LicenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para funcionalidades de cliente
 */
@RestController
@RequestMapping("/api/cliente")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class ClienteController {

    @Autowired
    private LicenciaService licenciaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtener las licencias del usuario autenticado
     * GET /api/cliente/mis-licencias
     */
    @GetMapping("/mis-licencias")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerMisLicencias() {
        try {
            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Obtener licencias activas del usuario
            List<LicenciaDTO> licencias = licenciaService.listarActivasPorUsuario(usuario.getId());
            
            return ResponseEntity.ok(licencias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener licencias: " + e.getMessage());
        }
    }

    /**
     * Obtener todas las aplicaciones disponibles con sus licencias
     * GET /api/cliente/mis-apps
     */
    @GetMapping("/mis-apps")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerMisAplicaciones() {
        try {
            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Obtener licencias del usuario (incluyen info de aplicación)
            List<LicenciaDTO> licencias = licenciaService.listarPorUsuario(usuario.getId());
            
            return ResponseEntity.ok(licencias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener aplicaciones: " + e.getMessage());
        }
    }
}