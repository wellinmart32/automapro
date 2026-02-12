package com.automapro.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que representa una aplicación/software disponible en el sistema
 */
@Entity
@Table(name = "aplicaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aplicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String version;

    @Column(name = "ruta_archivo", length = 500)
    private String rutaArchivo; // Ruta física del instalador en el servidor

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl; // URL o ruta de la imagen de la app

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}