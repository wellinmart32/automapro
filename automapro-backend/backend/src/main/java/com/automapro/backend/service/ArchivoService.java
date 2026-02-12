package com.automapro.backend.service;

import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.entity.Descarga;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.repository.DescargaRepository;
import com.automapro.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Servicio para gestionar la subida y descarga de archivos (instaladores)
 */
@Service
public class ArchivoService {

    @Value("${ruta.almacenamiento.archivos}")
    private String rutaAlmacenamiento;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DescargaRepository descargaRepository;

    @Autowired
    private AplicacionService aplicacionService;

    /**
     * Inicializar el directorio de almacenamiento
     */
    public void init() {
        try {
            Path ruta = Paths.get(rutaAlmacenamiento);
            if (!Files.exists(ruta)) {
                Files.createDirectories(ruta);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento: " + e.getMessage());
        }
    }

    /**
     * Guardar un archivo instalador para una aplicación
     */
    public String guardarArchivo(Long aplicacionId, MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        try {
            // Verificar que la aplicación existe
            Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            // Crear directorio si no existe
            init();

            // Generar nombre único del archivo
            String nombreOriginal = archivo.getOriginalFilename();
            String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            String nombreArchivo = "app_" + aplicacionId + "_" + System.currentTimeMillis() + extension;

            // Guardar el archivo
            Path rutaDestino = Paths.get(rutaAlmacenamiento).resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            // Actualizar la ruta del archivo en la aplicación
            String rutaRelativa = nombreArchivo;
            aplicacionService.actualizarRutaArchivo(aplicacionId, rutaRelativa);

            return rutaRelativa;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }

    /**
     * Cargar un archivo como recurso para descarga
     */
    public Resource cargarArchivo(String nombreArchivo) {
        try {
            Path rutaArchivo = Paths.get(rutaAlmacenamiento).resolve(nombreArchivo).normalize();
            Resource recurso = new UrlResource(rutaArchivo.toUri());

            if (recurso.exists() && recurso.isReadable()) {
                return recurso;
            } else {
                throw new RuntimeException("No se pudo leer el archivo: " + nombreArchivo);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al cargar el archivo: " + e.getMessage());
        }
    }

    /**
     * Registrar una descarga en el historial
     */
    public void registrarDescarga(Long usuarioId, Long aplicacionId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

        Descarga descarga = new Descarga();
        descarga.setUsuario(usuario);
        descarga.setAplicacion(aplicacion);

        descargaRepository.save(descarga);
    }

    /**
     * Eliminar un archivo del sistema
     */
    public void eliminarArchivo(String nombreArchivo) {
        try {
            Path rutaArchivo = Paths.get(rutaAlmacenamiento).resolve(nombreArchivo).normalize();
            Files.deleteIfExists(rutaArchivo);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo: " + e.getMessage());
        }
    }
}