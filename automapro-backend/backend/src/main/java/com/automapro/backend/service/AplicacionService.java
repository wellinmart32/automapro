package com.automapro.backend.service;

import com.automapro.backend.dto.AplicacionDTO;
import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.repository.AplicacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones CRUD de aplicaciones
 */
@Service
public class AplicacionService {

    @Autowired
    private AplicacionRepository aplicacionRepository;

    /**
     * Listar todas las aplicaciones
     */
    public List<AplicacionDTO> listarTodas() {
        return aplicacionRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar solo aplicaciones activas
     */
    public List<AplicacionDTO> listarActivas() {
        return aplicacionRepository.findByActivo(true).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener una aplicación por ID
     */
    public AplicacionDTO obtenerPorId(Long id) {
        Aplicacion aplicacion = aplicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
        return convertirADTO(aplicacion);
    }

    /**
     * Buscar aplicaciones por nombre
     */
    public List<AplicacionDTO> buscarPorNombre(String nombre) {
        return aplicacionRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Crear una nueva aplicación
     */
    public AplicacionDTO crear(AplicacionDTO aplicacionDTO) {
        // Verificar si ya existe una aplicación con ese nombre
        if (aplicacionRepository.existsByNombre(aplicacionDTO.getNombre())) {
            throw new RuntimeException("Ya existe una aplicación con ese nombre");
        }

        Aplicacion aplicacion = new Aplicacion();
        aplicacion.setNombre(aplicacionDTO.getNombre());
        aplicacion.setDescripcion(aplicacionDTO.getDescripcion());
        aplicacion.setVersion(aplicacionDTO.getVersion());
        aplicacion.setImagenUrl(aplicacionDTO.getImagenUrl());
        aplicacion.setActivo(aplicacionDTO.getActivo() != null ? aplicacionDTO.getActivo() : true);

        Aplicacion guardada = aplicacionRepository.save(aplicacion);
        return convertirADTO(guardada);
    }

    /**
     * Actualizar una aplicación existente
     */
    public AplicacionDTO actualizar(Long id, AplicacionDTO aplicacionDTO) {
        Aplicacion aplicacion = aplicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

        aplicacion.setNombre(aplicacionDTO.getNombre());
        aplicacion.setDescripcion(aplicacionDTO.getDescripcion());
        aplicacion.setVersion(aplicacionDTO.getVersion());
        aplicacion.setImagenUrl(aplicacionDTO.getImagenUrl());
        aplicacion.setActivo(aplicacionDTO.getActivo());

        Aplicacion actualizada = aplicacionRepository.save(aplicacion);
        return convertirADTO(actualizada);
    }

    /**
     * Actualizar la ruta del archivo instalador
     */
    public void actualizarRutaArchivo(Long id, String rutaArchivo) {
        Aplicacion aplicacion = aplicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
        aplicacion.setRutaArchivo(rutaArchivo);
        aplicacionRepository.save(aplicacion);
    }

    /**
     * Eliminar una aplicación (borrado lógico)
     */
    public void eliminar(Long id) {
        Aplicacion aplicacion = aplicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
        aplicacion.setActivo(false);
        aplicacionRepository.save(aplicacion);
    }

    /**
     * Convertir entidad Aplicacion a DTO
     */
    private AplicacionDTO convertirADTO(Aplicacion aplicacion) {
        return new AplicacionDTO(
                aplicacion.getId(),
                aplicacion.getNombre(),
                aplicacion.getDescripcion(),
                aplicacion.getVersion(),
                aplicacion.getRutaArchivo(),
                aplicacion.getImagenUrl(),
                aplicacion.getActivo(),
                aplicacion.getFechaCreacion()
        );
    }
}