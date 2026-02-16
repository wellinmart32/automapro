package com.automapro.backend.service;

import com.automapro.backend.dto.LicenciaDTO;
import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.entity.Licencia;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.repository.LicenciaRepository;
import com.automapro.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones CRUD de licencias
 */
@Service
public class LicenciaService {

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    /**
     * Listar todas las licencias
     */
    public List<LicenciaDTO> listarTodas() {
        return licenciaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar licencias de un usuario específico
     */
    public List<LicenciaDTO> listarPorUsuario(Long usuarioId) {
        return licenciaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar licencias activas de un usuario
     */
    public List<LicenciaDTO> listarActivasPorUsuario(Long usuarioId) {
        return licenciaRepository.findByUsuarioIdAndActivo(usuarioId, true).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener una licencia por ID
     */
    public LicenciaDTO obtenerPorId(Long id) {
        Licencia licencia = licenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Licencia no encontrada"));
        return convertirADTO(licencia);
    }

    /**
     * Crear una nueva licencia
     */
    public LicenciaDTO crear(LicenciaDTO licenciaDTO) {
        // Verificar usuario
        Usuario usuario = usuarioRepository.findById(licenciaDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar aplicación
        Aplicacion aplicacion = aplicacionRepository.findById(licenciaDTO.getAplicacionId())
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

        // Verificar si ya existe licencia
        if (licenciaRepository.existsByUsuarioIdAndAplicacionId(licenciaDTO.getUsuarioId(), licenciaDTO.getAplicacionId())) {
            throw new RuntimeException("El usuario ya tiene una licencia para esta aplicación");
        }

        Licencia licencia = new Licencia();
        licencia.setUsuario(usuario);
        licencia.setAplicacion(aplicacion);
        licencia.setCodigo(licenciaDTO.getCodigo() != null ? licenciaDTO.getCodigo() : generarCodigoLicencia());
        licencia.setTipoLicencia(licenciaDTO.getTipoLicencia() != null ? licenciaDTO.getTipoLicencia() : "TRIAL");
        licencia.setDiasTrial(licenciaDTO.getDiasTrial() != null ? licenciaDTO.getDiasTrial() : aplicacion.getDiasTrial());
        licencia.setFechaExpiracion(licenciaDTO.getFechaExpiracion());
        licencia.setActivo(licenciaDTO.getActivo() != null ? licenciaDTO.getActivo() : true);

        Licencia guardada = licenciaRepository.save(licencia);
        return convertirADTO(guardada);
    }

    /**
     * Actualizar una licencia existente
     */
    public LicenciaDTO actualizar(Long id, LicenciaDTO licenciaDTO) {
        Licencia licencia = licenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Licencia no encontrada"));

        licencia.setTipoLicencia(licenciaDTO.getTipoLicencia());
        licencia.setFechaInicioUso(licenciaDTO.getFechaInicioUso());
        licencia.setDiasTrial(licenciaDTO.getDiasTrial());
        licencia.setFechaExpiracion(licenciaDTO.getFechaExpiracion());
        licencia.setActivo(licenciaDTO.getActivo());

        Licencia actualizada = licenciaRepository.save(licencia);
        return convertirADTO(actualizada);
    }

    /**
     * Eliminar una licencia (borrado lógico)
     */
    public void eliminar(Long id) {
        Licencia licencia = licenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Licencia no encontrada"));
        licencia.setActivo(false);
        licenciaRepository.save(licencia);
    }

    /**
     * Convertir licencia TRIAL a FULL después de pago exitoso
     */
    public void convertirAFull(Long licenciaId) {
        Licencia licencia = licenciaRepository.findById(licenciaId)
                .orElseThrow(() -> new RuntimeException("Licencia no encontrada"));
        
        // Cambiar tipo a FULL
        licencia.setTipoLicencia("FULL");
        // Eliminar fecha de expiración (licencia permanente)
        licencia.setFechaExpiracion(null);
        // Asegurar que esté activa
        licencia.setActivo(true);
        
        licenciaRepository.save(licencia);
    }

    /**
     * Generar código único de licencia
     */
    private String generarCodigoLicencia() {
        return "LIC-" + UUID.randomUUID().toString().toUpperCase().substring(0, 8);
    }

    /**
     * Convertir entidad Licencia a DTO
     */
    private LicenciaDTO convertirADTO(Licencia licencia) {
        LicenciaDTO dto = new LicenciaDTO();
        dto.setId(licencia.getId());
        dto.setUsuarioId(licencia.getUsuario().getId());
        dto.setUsuarioNombre(licencia.getUsuario().getNombre());
        dto.setUsuarioEmail(licencia.getUsuario().getEmail());
        dto.setAplicacionId(licencia.getAplicacion().getId());
        dto.setAplicacionNombre(licencia.getAplicacion().getNombre());
        dto.setAplicacionVersion(licencia.getAplicacion().getVersion());
        dto.setAplicacionRutaArchivo(licencia.getAplicacion().getRutaArchivo());
        dto.setCodigo(licencia.getCodigo());
        dto.setTipoLicencia(licencia.getTipoLicencia());
        dto.setFechaInicioUso(licencia.getFechaInicioUso());
        dto.setDiasTrial(licencia.getDiasTrial());
        dto.setFechaExpiracion(licencia.getFechaExpiracion());
        dto.setActivo(licencia.getActivo());
        dto.setFechaCreacion(licencia.getFechaCreacion());
        return dto;
    }
}