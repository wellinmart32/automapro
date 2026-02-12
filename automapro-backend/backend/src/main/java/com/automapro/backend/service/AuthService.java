package com.automapro.backend.service;

import com.automapro.backend.dto.LoginRequest;
import com.automapro.backend.dto.LoginResponse;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.repository.UsuarioRepository;
import com.automapro.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar la autenticación y registro de usuarios
 */
@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Autenticar un usuario y generar token JWT
     */
    public LoginResponse login(LoginRequest loginRequest) {
        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Obtener UserDetails del usuario autenticado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generar token JWT
        String token = jwtUtil.generateToken(userDetails);

        // Buscar datos adicionales del usuario
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Retornar respuesta con token y datos del usuario
        return new LoginResponse(token, usuario.getEmail(), usuario.getNombre(), usuario.getRol());
    }

    /**
     * Registrar un nuevo usuario (solo puede ser invocado por un ADMIN)
     */
    public Usuario registrarUsuario(String nombre, String email, String password, String rol) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password)); // Encriptar password
        usuario.setRol(rol);
        usuario.setActivo(true);

        // Guardar en la base de datos
        return usuarioRepository.save(usuario);
    }
}