package com.automapro.backend.repository;

import com.automapro.backend.entity.Descarga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para operaciones CRUD de Descarga
 */
@Repository
public interface DescargaRepository extends JpaRepository<Descarga, Long> {
    
    // Buscar descargas de un usuario específico
    List<Descarga> findByUsuarioId(Long usuarioId);
    
    // Buscar descargas de una aplicación específica
    List<Descarga> findByAplicacionId(Long aplicacionId);
    
    // Buscar descargas de un usuario para una aplicación específica
    List<Descarga> findByUsuarioIdAndAplicacionId(Long usuarioId, Long aplicacionId);
    
    // Contar descargas de una aplicación
    long countByAplicacionId(Long aplicacionId);
}