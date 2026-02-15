package com.automapro.backend.controller;

import com.automapro.backend.dto.AplicacionDTO;
import com.automapro.backend.dto.LicenciaDTO;
import com.automapro.backend.entity.Licencia;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.repository.LicenciaRepository;
import com.automapro.backend.repository.UsuarioRepository;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.service.AplicacionService;
import com.automapro.backend.service.LicenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador público (sin autenticación) para catálogo de aplicaciones y verificación de licencias
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class PublicController {

    @Autowired
    private AplicacionService aplicacionService;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Autowired
    private LicenciaService licenciaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    /**
     * Obtener catálogo de aplicaciones activas (sin autenticación)
     * GET /api/public/aplicaciones
     */
    @GetMapping("/aplicaciones")
    public ResponseEntity<List<AplicacionDTO>> obtenerCatalogo() {
        List<AplicacionDTO> aplicaciones = aplicacionService.listarActivas();
        return ResponseEntity.ok(aplicaciones);
    }

    /**
     * Obtener detalle de una aplicación específica (sin autenticación)
     * GET /api/public/aplicaciones/{id}
     */
    @GetMapping("/aplicaciones/{id}")
    public ResponseEntity<?> obtenerAplicacion(@PathVariable Long id) {
        try {
            AplicacionDTO aplicacion = aplicacionService.obtenerPorId(id);
            return ResponseEntity.ok(aplicacion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Aplicación no encontrada");
        }
    }

    /**
     * Generar licencia TRIAL automáticamente (requiere autenticación)
     * POST /api/public/generar-licencia-trial/{aplicacionId}
     */
    @PostMapping("/generar-licencia-trial/{aplicacionId}")
    public ResponseEntity<?> generarLicenciaTrial(@PathVariable Long aplicacionId) {
        try {
            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar que la aplicación existe
            Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            // Verificar si el usuario ya tiene una licencia para esta aplicación
            if (licenciaRepository.existsByUsuarioIdAndAplicacionId(usuario.getId(), aplicacionId)) {
                return ResponseEntity.badRequest().body("Ya tienes una licencia para esta aplicación");
            }

            // Crear licencia TRIAL
            LicenciaDTO licenciaDTO = new LicenciaDTO();
            licenciaDTO.setUsuarioId(usuario.getId());
            licenciaDTO.setAplicacionId(aplicacionId);
            licenciaDTO.setTipoLicencia("TRIAL");
            licenciaDTO.setDiasTrial(aplicacion.getDiasTrial() != null ? aplicacion.getDiasTrial() : 30);
            licenciaDTO.setActivo(true);

            // Generar código automáticamente (el servicio lo hace)
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

    /**
     * Verificar estado de una licencia (usado por las aplicaciones)
     * GET /api/public/verificar-licencia/{codigo}
     */
    @GetMapping("/verificar-licencia/{codigo}")
    public ResponseEntity<?> verificarLicencia(@PathVariable String codigo) {
        try {
            Licencia licencia = licenciaRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new RuntimeException("Licencia no encontrada"));

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("valida", true);
            respuesta.put("tipo", licencia.getTipoLicencia());
            respuesta.put("activa", licencia.getActivo());
            respuesta.put("aplicacion", licencia.getAplicacion().getNombre());
            respuesta.put("version", licencia.getAplicacion().getVersion());

            // Si es tipo TRIAL, calcular días restantes
            if ("TRIAL".equals(licencia.getTipoLicencia())) {
                if (licencia.getFechaInicioUso() == null) {
                    // Primera vez que se usa, registrar fecha de inicio
                    licencia.setFechaInicioUso(LocalDate.now());
                    
                    // Calcular fecha de expiración basada en días trial
                    if (licencia.getDiasTrial() != null && licencia.getDiasTrial() > 0) {
                        licencia.setFechaExpiracion(LocalDate.now().plusDays(licencia.getDiasTrial()));
                    }
                    
                    licenciaRepository.save(licencia);
                }

                // Calcular días restantes
                if (licencia.getFechaExpiracion() != null) {
                    long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), licencia.getFechaExpiracion());
                    respuesta.put("diasRestantes", diasRestantes > 0 ? diasRestantes : 0);
                    respuesta.put("expirado", diasRestantes <= 0);
                    respuesta.put("fechaExpiracion", licencia.getFechaExpiracion());
                } else {
                    respuesta.put("diasRestantes", licencia.getDiasTrial());
                    respuesta.put("expirado", false);
                }
            } else if ("FULL".equals(licencia.getTipoLicencia())) {
                respuesta.put("permanente", true);
                respuesta.put("expirado", false);
            }

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valida", false);
            error.put("mensaje", "Licencia no válida o no encontrada");
            return ResponseEntity.badRequest().body(error);
        }
    }
}