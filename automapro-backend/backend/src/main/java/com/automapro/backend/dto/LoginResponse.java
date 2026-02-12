package com.automapro.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta del login (incluye el token JWT)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tipo = "Bearer";
    private String email;
    private String nombre;
    private String rol;

    // Constructor sin el tipo (se establece por defecto)
    public LoginResponse(String token, String email, String nombre, String rol) {
        this.token = token;
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;
    }
}