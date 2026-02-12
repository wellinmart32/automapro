package com.automapro.backend.controller;

import com.automapro.backend.dto.LoginRequest;
import com.automapro.backend.dto.LoginResponse;
import com.automapro.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para autenticación y registro de usuarios
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Endpoint para login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en el login: " + e.getMessage());
        }
    }

    /**
     * Endpoint para verificar si el token es válido (opcional)
     * GET /api/auth/verificar
     */
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarToken() {
        return ResponseEntity.ok("Token válido");
    }
}