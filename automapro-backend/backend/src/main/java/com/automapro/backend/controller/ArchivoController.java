package com.automapro.backend.controller;

import com.automapro.backend.service.AplicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para gestión de archivos (subida y descarga de instaladores)
 */
@RestController
@RequestMapping("/api/archivos")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class ArchivoController {

    // Directorio donde se guardan los instaladores
    private final Path directorioInstaladores = Paths.get("instaladores");

    @Autowired
    private AplicacionService aplicacionService;

    /**
     * Constructor - Crea el directorio si no existe
     */
    public ArchivoController() {
        try {
            Files.createDirectories(directorioInstaladores);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio para instaladores", e);
        }
    }

    /**
     * Subir instalador para una aplicación (solo ADMIN)
     * POST /api/archivos/subir/{aplicacionId}
     */
    @PostMapping("/subir/{aplicacionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> subirArchivo(
            @PathVariable Long aplicacionId,
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            // Validar que el archivo no esté vacío
            if (archivo.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "El archivo está vacío");
                return ResponseEntity.badRequest().body(error);
            }

            // Generar nombre único para el archivo
            String nombreOriginal = archivo.getOriginalFilename();
            String extension = nombreOriginal != null && nombreOriginal.contains(".") 
                ? nombreOriginal.substring(nombreOriginal.lastIndexOf(".")) 
                : "";
            String nombreArchivo = "app_" + aplicacionId + "_" + System.currentTimeMillis() + extension;

            // Guardar el archivo
            Path rutaDestino = directorioInstaladores.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            // Actualizar la ruta en la base de datos
            String rutaRelativa = "instaladores/" + nombreArchivo;
            aplicacionService.actualizarRutaArchivo(aplicacionId, rutaRelativa);

            // Crear respuesta JSON
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Archivo subido exitosamente");
            respuesta.put("nombreArchivo", nombreArchivo);
            return ResponseEntity.ok(respuesta);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al guardar el archivo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Descargar instalador (requiere autenticación)
     * GET /api/archivos/descargar/{nombreArchivo}
     */
    @GetMapping("/descargar/{nombreArchivo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable String nombreArchivo) {
        try {
            // Construir la ruta del archivo
            Path rutaArchivo = directorioInstaladores.resolve(nombreArchivo).normalize();
            Resource recurso = new UrlResource(rutaArchivo.toUri());

            // Verificar que el archivo existe y es legible
            if (!recurso.exists() || !recurso.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determinar el tipo de contenido
            String tipoContenido = Files.probeContentType(rutaArchivo);
            if (tipoContenido == null) {
                tipoContenido = "application/octet-stream";
            }

            // Retornar el archivo para descarga
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(tipoContenido))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + recurso.getFilename() + "\"")
                    .body(recurso);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}