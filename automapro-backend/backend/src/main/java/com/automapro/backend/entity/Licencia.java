package com.automapro.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa una licencia asignada a un usuario para una aplicación
 */
@Entity
@Table(name = "licencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Licencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id", nullable = false)
    private Aplicacion aplicacion;

    @Column(nullable = false, unique = true, length = 100)
    private String codigo; // Código único de licencia

    @Column(name = "tipo_licencia", nullable = false, length = 20)
    private String tipoLicencia; // TRIAL o FULL

    @Column(name = "fecha_inicio_uso")
    private LocalDate fechaInicioUso; // Fecha en que se usó la licencia por primera vez

    @Column(name = "dias_trial")
    private Integer diasTrial; // Cantidad de días del trial (normalmente 30)

    @Column(name = "fecha_expiracion")
    private LocalDate fechaExpiracion; // Fecha de expiración (para TRIAL o licencias anuales)

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}