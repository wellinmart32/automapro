package com.automapro.backend.controller;

import com.automapro.backend.dto.AplicacionDTO;
import com.automapro.backend.entity.Licencia;
import com.automapro.backend.repository.LicenciaRepository;
import com.automapro.backend.service.AplicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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