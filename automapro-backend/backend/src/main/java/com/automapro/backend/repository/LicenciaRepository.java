package com.automapro.backend.repository;

import com.automapro.backend.entity.Licencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de Licencia
 */
@Repository
public interface LicenciaRepository extends JpaRepository<Licencia, Long> {
    
    // Buscar licencias de un usuario específico
    List<Licencia> findByUsuarioId(Long usuarioId);
    
    // Buscar licencias activas de un usuario
    List<Licencia> findByUsuarioIdAndActivo(Long usuarioId, Boolean activo);
    
    // Buscar licencia por código
    Optional<Licencia> findByCodigo(String codigo);
    
    // Verificar si existe una licencia para usuario y aplicación
    boolean existsByUsuarioIdAndAplicacionId(Long usuarioId, Long aplicacionId);
    
    // Buscar licencia específica por usuario y aplicación (NUEVO)
    Optional<Licencia> findByUsuarioIdAndAplicacionId(Long usuarioId, Long aplicacionId);
    
    // Buscar licencias de una aplicación específica
    List<Licencia> findByAplicacionId(Long aplicacionId);
}