package com.automapro.backend.controller;

import com.automapro.backend.dto.LoginRequest;
import com.automapro.backend.dto.LoginResponse;
import com.automapro.backend.dto.UsuarioDTO;
import com.automapro.backend.entity.PasswordResetToken;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.repository.PasswordResetTokenRepository;
import com.automapro.backend.repository.UsuarioRepository;
import com.automapro.backend.service.AuthService;
import com.automapro.backend.service.EmailService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador para autenticación y registro de usuarios
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    /**
     * Solicitar recuperación de contraseña
     * POST /api/auth/recuperar-password
     */
    @Transactional
    @PostMapping("/recuperar-password")
    public ResponseEntity<?> recuperarPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El email es requerido");
            }

            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim());
            if (usuarioOpt.isEmpty()) {
                // Por seguridad, responder igual aunque no exista
                return ResponseEntity.ok("Si el email existe, recibirás un enlace de recuperación");
            }

            Usuario usuario = usuarioOpt.get();

            // Eliminar tokens anteriores del usuario
            passwordResetTokenRepository.deleteByUsuarioId(usuario.getId());

            // Generar nuevo token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUsuario(usuario);
            resetToken.setToken(token);
            resetToken.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
            resetToken.setUsado(false);
            passwordResetTokenRepository.save(resetToken);

            // Enviar email
            emailService.enviarEmailRecuperacion(usuario.getEmail(), usuario.getNombre(), token);

            return ResponseEntity.ok("Si el email existe, recibirás un enlace de recuperación");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar solicitud: " + e.getMessage());
        }
    }

    /**
     * Restablecer contraseña con token
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String nuevaPassword = request.get("password");

            if (token == null || nuevaPassword == null) {
                return ResponseEntity.badRequest().body("Datos incompletos");
            }

            if (nuevaPassword.length() < 6) {
                return ResponseEntity.badRequest().body("La contraseña debe tener al menos 6 caracteres");
            }

            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

            if (resetToken.getUsado()) {
                return ResponseEntity.badRequest().body("Este enlace ya fue utilizado");
            }

            if (resetToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("El enlace ha expirado. Solicita uno nuevo");
            }

            Usuario usuario = resetToken.getUsuario();
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
            usuarioRepository.save(usuario);

            resetToken.setUsado(true);
            passwordResetTokenRepository.save(resetToken);

            return ResponseEntity.ok("Contraseña actualizada correctamente");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}