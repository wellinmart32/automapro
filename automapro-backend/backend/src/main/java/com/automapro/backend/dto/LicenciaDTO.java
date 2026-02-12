package com.automapro.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Licencia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenciaDTO {

    private Long id;

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long usuarioId;

    private String usuarioNombre; // Para mostrar en el frontend
    private String usuarioEmail;

    @NotNull(message = "El ID de aplicación es obligatorio")
    private Long aplicacionId;

    private String aplicacionNombre; // Para mostrar en el frontend
    private String aplicacionVersion;

    @NotBlank(message = "El código de licencia es obligatorio")
    private String codigo;

    private LocalDate fechaExpiracion; // Null = licencia permanente

    private Boolean activo;

    private LocalDateTime fechaCreacion;

    // Constructor simplificado para crear nuevas licencias
    public LicenciaDTO(Long usuarioId, Long aplicacionId, String codigo, LocalDate fechaExpiracion, Boolean activo) {
        this.usuarioId = usuarioId;
        this.aplicacionId = aplicacionId;
        this.codigo = codigo;
        this.fechaExpiracion = fechaExpiracion;
        this.activo = activo;
    }
}