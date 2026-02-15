package com.automapro.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Aplicacion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AplicacionDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "La versión es obligatoria")
    @Size(max = 50, message = "La versión no puede exceder 50 caracteres")
    private String version;

    private String rutaArchivo;

    private String imagenUrl;

    private BigDecimal precio; // Precio de la versión completa

    private Integer diasTrial; // Días del período de prueba

    private Boolean activo;

    private LocalDateTime fechaCreacion;

    // Constructor sin ID y sin rutaArchivo (para crear nuevas aplicaciones)
    public AplicacionDTO(String nombre, String descripcion, String version, String imagenUrl, BigDecimal precio, Integer diasTrial, Boolean activo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.version = version;
        this.imagenUrl = imagenUrl;
        this.precio = precio;
        this.diasTrial = diasTrial;
        this.activo = activo;
    }
}