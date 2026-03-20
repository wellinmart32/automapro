package com.automapro.backend.controller;

import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.service.AplicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/archivos")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class ArchivoController {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Value("${supabase.storage.bucket}")
    private String supabaseBucket;

    @Autowired
    private AplicacionService aplicacionService;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Subir instalador a Supabase Storage (solo ADMIN)
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

            Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            // Eliminar archivo anterior de Supabase si existe
            if (aplicacion.getRutaArchivo() != null && !aplicacion.getRutaArchivo().isEmpty()) {
                eliminarDeSupabase(aplicacion.getRutaArchivo());
            }

            // Generar nombre único
            String nombreOriginal = archivo.getOriginalFilename();
            String extension = (nombreOriginal != null && nombreOriginal.contains("."))
                    ? nombreOriginal.substring(nombreOriginal.lastIndexOf("."))
                    : ".exe";
            String nombreArchivo = "app_" + aplicacionId + "_" + System.currentTimeMillis() + extension;

            // Subir a Supabase Storage
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + nombreArchivo;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(archivo.getBytes(), headers);
            ResponseEntity<String> supabaseResponse = restTemplate.exchange(
                    uploadUrl, HttpMethod.POST, requestEntity, String.class);

            if (!supabaseResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al subir a Supabase: " + supabaseResponse.getBody());
            }

            // Guardar nombre del archivo en la BD
            aplicacionService.actualizarRutaArchivo(aplicacionId, nombreArchivo);

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Archivo subido exitosamente");
            respuesta.put("nombreArchivo", nombreArchivo);
            return ResponseEntity.ok(respuesta);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al procesar el archivo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Descargar instalador por ID de aplicación — redirige a URL de GitHub Releases
     */
    @GetMapping("/descargar/{aplicacionId}")
    public ResponseEntity<?> descargarArchivo(@PathVariable Long aplicacionId) {
        Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

        String urlDescarga = aplicacion.getRutaArchivo();

        if (urlDescarga == null || urlDescarga.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "No hay instalador disponible para esta aplicación");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, urlDescarga)
                .build();
    }

    /**
     * Eliminar archivo de Supabase Storage
     */
    private void eliminarDeSupabase(String nombreArchivo) {
        try {
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + nombreArchivo;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
        } catch (Exception e) {
            System.err.println("No se pudo eliminar archivo anterior de Supabase: " + e.getMessage());
        }
    }
}