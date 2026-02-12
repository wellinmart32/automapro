package com.automapro.backend.controller;

import com.automapro.backend.service.ArchivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador para gestión de archivos (subida y descarga de instaladores)
 */
@RestController
@RequestMapping("/api/admin/archivos")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
@PreAuthorize("hasRole('ADMIN')")
public class ArchivoController {

    @Autowired
    private ArchivoService archivoService;

    /**
     * Subir un archivo instalador para una aplicación
     * POST /api/admin/archivos/subir/{aplicacionId}
     */
    @PostMapping("/subir/{aplicacionId}")
    public ResponseEntity<?> subirArchivo(@PathVariable Long aplicacionId,
                                          @RequestParam("archivo") MultipartFile archivo) {
        try {
            String rutaArchivo = archivoService.guardarArchivo(aplicacionId, archivo);
            return ResponseEntity.ok("Archivo subido correctamente: " + rutaArchivo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al subir archivo: " + e.getMessage());
        }
    }

    /**
     * Descargar un archivo por su nombre
     * GET /api/admin/archivos/descargar/{nombreArchivo}
     */
    @GetMapping("/descargar/{nombreArchivo}")
    public ResponseEntity<?> descargarArchivo(@PathVariable String nombreArchivo) {
        try {
            Resource recurso = archivoService.cargarArchivo(nombreArchivo);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                    .body(recurso);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al descargar archivo: " + e.getMessage());
        }
    }

    /**
     * Eliminar un archivo del sistema
     * DELETE /api/admin/archivos/eliminar/{nombreArchivo}
     */
    @DeleteMapping("/eliminar/{nombreArchivo}")
    public ResponseEntity<?> eliminarArchivo(@PathVariable String nombreArchivo) {
        try {
            archivoService.eliminarArchivo(nombreArchivo);
            return ResponseEntity.ok("Archivo eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar archivo: " + e.getMessage());
        }
    }
}