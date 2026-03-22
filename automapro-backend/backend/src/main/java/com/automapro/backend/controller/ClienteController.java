package com.automapro.backend.controller;

import com.automapro.backend.dto.LicenciaDTO;
import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.repository.LicenciaRepository;
import com.automapro.backend.repository.UsuarioRepository;
import com.automapro.backend.service.LicenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private AplicacionRepository aplicacionRepository;

    @Autowired
    private LicenciaRepository licenciaRepository;

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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            List<LicenciaDTO> licencias = licenciaService.listarPorUsuario(usuario.getId());
            
            return ResponseEntity.ok(licencias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener aplicaciones: " + e.getMessage());
        }
    }

    /**
     * Generar licencia TRIAL automáticamente (requiere autenticación)
     * POST /api/cliente/generar-licencia-trial/{aplicacionId}
     */
    @PostMapping("/generar-licencia-trial/{aplicacionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> generarLicenciaTrial(@PathVariable Long aplicacionId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            if (licenciaRepository.existsByUsuarioIdAndAplicacionId(usuario.getId(), aplicacionId)) {
                return ResponseEntity.badRequest().body("Ya tienes una licencia para esta aplicación");
            }

            LicenciaDTO licenciaDTO = new LicenciaDTO();
            licenciaDTO.setUsuarioId(usuario.getId());
            licenciaDTO.setAplicacionId(aplicacionId);
            licenciaDTO.setTipoLicencia("TRIAL");
            licenciaDTO.setDiasTrial(aplicacion.getDiasTrial() != null ? aplicacion.getDiasTrial() : 30);
            licenciaDTO.setActivo(true);

            LicenciaDTO licenciaCreada = licenciaService.crear(licenciaDTO);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Licencia TRIAL generada exitosamente");
            respuesta.put("licencia", licenciaCreada);
            respuesta.put("aplicacion", aplicacion.getNombre());
            respuesta.put("diasTrial", licenciaCreada.getDiasTrial());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar licencia: " + e.getMessage());
        }
    }
}