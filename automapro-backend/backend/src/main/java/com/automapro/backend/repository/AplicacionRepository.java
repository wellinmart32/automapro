package com.automapro.backend.repository;

import com.automapro.backend.entity.Aplicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para operaciones CRUD de Aplicacion
 */
@Repository
public interface AplicacionRepository extends JpaRepository<Aplicacion, Long> {
    
    // Buscar aplicaciones activas
    List<Aplicacion> findByActivo(Boolean activo);
    
    // Buscar aplicaciones por nombre (búsqueda parcial)
    List<Aplicacion> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar aplicación por nombre exacto
    boolean existsByNombre(String nombre);

    // Buscar aplicación por ID de producto en Hotmart (para el webhook)
    java.util.Optional<Aplicacion> findByHotmartProductId(String hotmartProductId);
}