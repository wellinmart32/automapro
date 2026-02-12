package com.automapro.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Usuario (sin exponer el password)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @NotBlank(message = "El rol es obligatorio")
    private String rol; // ROLE_ADMIN o ROLE_CLIENTE

    private Boolean activo;

    private LocalDateTime fechaCreacion;

    // Constructor sin ID (para crear nuevos usuarios)
    public UsuarioDTO(String nombre, String email, String rol, Boolean activo) {
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
    }
}