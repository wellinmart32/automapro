package com.automapro.backend.controller;

import com.automapro.backend.dto.LicenciaDTO;
import com.automapro.backend.service.ArchivoService;
import com.automapro.backend.service.LicenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para funcionalidades de clientes (ver aplicaciones y descargar)
 */
@RestController
@RequestMapping("/api/cliente")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
public class ClienteController {

    @Autowired
    private LicenciaService licenciaService;

    @Autowired
    private ArchivoService archivoService;

    /**
     * Obtener las aplicaciones (licencias) del cliente autenticado
     * GET /api/cliente/mis-apps
     */
    @GetMapping("/mis-apps")
    public ResponseEntity<?> obtenerMisAplicaciones() {
        try {
            // Obtener el email del usuario autenticado desde el contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // TODO: Obtener el ID del usuario desde el email
            // Por ahora retornamos un mensaje de ejemplo
            // Long usuarioId = obtenerIdUsuarioPorEmail(email);
            // List<LicenciaDTO> licencias = licenciaService.listarActivasPorUsuario(usuarioId);
            
            return ResponseEntity.ok("Endpoint mis-apps funcionando para usuario: " + email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener aplicaciones: " + e.getMessage());
        }
    }

    /**
     * Descargar un instalador de una aplicación
     * GET /api/cliente/descargar/{aplicacionId}
     */
    @GetMapping("/descargar/{aplicacionId}")
    public ResponseEntity<?> descargarAplicacion(@PathVariable Long aplicacionId) {
        try {
            // TODO: Verificar que el usuario tiene licencia para esta aplicación
            // TODO: Obtener la ruta del archivo desde la aplicación
            // TODO: Registrar la descarga
            
            String nombreArchivo = "ejemplo.exe"; // Temporal
            Resource recurso = archivoService.cargarArchivo(nombreArchivo);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                    .body(recurso);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al descargar aplicación: " + e.getMessage());
        }
    }
}