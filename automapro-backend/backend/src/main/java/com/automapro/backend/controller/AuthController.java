package com.automapro.backend.controller;

import com.automapro.backend.dto.LoginRequest;
import com.automapro.backend.dto.LoginResponse;
import com.automapro.backend.dto.UsuarioDTO;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
     * Endpoint para registro público de nuevos usuarios (CLIENTES)
     * POST /api/auth/registro
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> request) {
        try {
            String nombre = request.get("nombre");
            String email = request.get("email");
            String password = request.get("password");
            
            // Validaciones básicas
            if (nombre == null || nombre.isEmpty() || 
                email == null || email.isEmpty() || 
                password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body("Todos los campos son obligatorios");
            }
            
            // Registrar usuario como CLIENTE
            Usuario usuario = authService.registrarUsuario(nombre, email, password, "ROLE_CLIENTE");
            
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar usuario: " + e.getMessage());
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