package com.automapro.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que registra el historial de descargas de aplicaciones por usuarios
 */
@Entity
@Table(name = "descargas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Descarga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id", nullable = false)
    private Aplicacion aplicacion;

    @Column(name = "fecha_descarga", nullable = false, updatable = false)
    private LocalDateTime fechaDescarga;

    @PrePersist
    protected void onCreate() {
        fechaDescarga = LocalDateTime.now();
    }
}