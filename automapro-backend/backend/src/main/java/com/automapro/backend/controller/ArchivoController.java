package com.automapro.backend.controller;

import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.repository.AplicacionRepository;
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

@RestController
@RequestMapping("/api/archivos")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class ArchivoController {

    private final Path directorioInstaladores = Paths.get("instaladores");

    @Autowired
    private AplicacionService aplicacionService;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    public ArchivoController() {
        try {
            Files.createDirectories(directorioInstaladores);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio para instaladores", e);
        }
    }

    /**
     * Subir instalador para una aplicación (solo ADMIN)
     */
    @PostMapping("/subir/{aplicacionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> subirArchivo(
            @PathVariable Long aplicacionId,
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            if (archivo.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "El archivo está vacío");
                return ResponseEntity.badRequest().body(error);
            }

            // Buscar aplicación
            Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            // ELIMINAR ARCHIVO ANTERIOR si existe
            if (aplicacion.getRutaArchivo() != null && !aplicacion.getRutaArchivo().isEmpty()) {
                String nombreArchivoViejo = aplicacion.getRutaArchivo().replace("instaladores/", "");
                Path rutaArchivoViejo = directorioInstaladores.resolve(nombreArchivoViejo);
                
                try {
                    Files.deleteIfExists(rutaArchivoViejo);
                    System.out.println("Archivo anterior eliminado: " + nombreArchivoViejo);
                } catch (IOException e) {
                    System.err.println("No se pudo eliminar archivo anterior: " + e.getMessage());
                }
            }

            // Generar nombre único para el nuevo archivo
            String nombreOriginal = archivo.getOriginalFilename();
            String extension = nombreOriginal != null && nombreOriginal.contains(".") 
                ? nombreOriginal.substring(nombreOriginal.lastIndexOf(".")) 
                : "";
            String nombreArchivo = "app_" + aplicacionId + "_" + System.currentTimeMillis() + extension;

            // Guardar el nuevo archivo
            Path rutaDestino = directorioInstaladores.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            // Actualizar ruta en la BD
            String rutaRelativa = "instaladores/" + nombreArchivo;
            aplicacionService.actualizarRutaArchivo(aplicacionId, rutaRelativa);

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
     * Descargar instalador (público - sin autenticación)
     */
    @GetMapping("/descargar/{nombreArchivo}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable String nombreArchivo) {
        try {
            Path rutaArchivo = directorioInstaladores.resolve(nombreArchivo).normalize();
            Resource recurso = new UrlResource(rutaArchivo.toUri());

            if (!recurso.exists() || !recurso.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String tipoContenido = Files.probeContentType(rutaArchivo);
            if (tipoContenido == null) {
                tipoContenido = "application/octet-stream";
            }

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