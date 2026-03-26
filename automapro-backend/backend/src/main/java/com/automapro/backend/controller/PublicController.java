package com.automapro.backend.controller;

import com.automapro.backend.dto.AplicacionDTO;
import com.automapro.backend.dto.LicenciaDTO;
import com.automapro.backend.entity.Licencia;
import com.automapro.backend.entity.Usuario;
import com.automapro.backend.entity.Aplicacion;
import com.automapro.backend.repository.LicenciaRepository;
import com.automapro.backend.repository.UsuarioRepository;
import com.automapro.backend.repository.AplicacionRepository;
import com.automapro.backend.service.AplicacionService;
import com.automapro.backend.service.LicenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador público (sin autenticación) para catálogo de aplicaciones y verificación de licencias
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "${cors.origenes.permitidos}")
public class PublicController {

    @Autowired
    private AplicacionService aplicacionService;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Autowired
    private LicenciaService licenciaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    /**
     * Obtener catálogo de aplicaciones activas (sin autenticación)
     * GET /api/public/aplicaciones
     */
    @GetMapping("/aplicaciones")
    public ResponseEntity<List<AplicacionDTO>> obtenerCatalogo() {
        List<AplicacionDTO> aplicaciones = aplicacionService.listarActivas();
        return ResponseEntity.ok(aplicaciones);
    }

    /**
     * Verificar versión actual de una aplicación por nombre (sin autenticación)
     * GET /api/public/version?nombre=MensajesBiblicos
     */
    @GetMapping("/version")
    public ResponseEntity<?> verificarVersion(@RequestParam String nombre) {
        return aplicacionRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .filter(a -> a.getActivo())
                .findFirst()
                .map(app -> {
                    Map<String, String> respuesta = new HashMap<>();
                    respuesta.put("nombre", app.getNombre());
                    respuesta.put("version", app.getVersion());
                    respuesta.put("rutaArchivo", app.getRutaArchivo() != null ? app.getRutaArchivo() : "");
                    return ResponseEntity.ok(respuesta);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtener detalle de una aplicación específica (sin autenticación)
     * GET /api/public/aplicaciones/{id}
     */
    @GetMapping("/aplicaciones/{id}")
    public ResponseEntity<?> obtenerAplicacion(@PathVariable Long id) {
        try {
            AplicacionDTO aplicacion = aplicacionService.obtenerPorId(id);
            return ResponseEntity.ok(aplicacion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Aplicación no encontrada");
        }
    }

    /**
     * Registrar instalación — crea o recupera licencia TRIAL por UUID de dispositivo
     * POST /api/public/registrar-instalacion
     */
    @PostMapping("/registrar-instalacion")
    public ResponseEntity<?> registrarInstalacion(@RequestBody Map<String, String> request) {
        try {
            String deviceUuid = request.get("deviceUuid");
            String nombreApp = request.get("nombreApp");

            if (deviceUuid == null || nombreApp == null) {
                return ResponseEntity.badRequest().body("Datos incompletos");
            }

            // Buscar la aplicación
            Aplicacion aplicacion = aplicacionRepository
                    .findByNombreContainingIgnoreCase(nombreApp)
                    .stream().filter(Aplicacion::getActivo).findFirst()
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            // Buscar licencia existente por deviceUuid
            java.util.Optional<Licencia> licenciaExistente =
                    licenciaRepository.findByDeviceUuidAndAplicacionId(deviceUuid, aplicacion.getId());

            if (licenciaExistente.isPresent()) {
                Licencia lic = licenciaExistente.get();
                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("codigo", lic.getCodigo());
                respuesta.put("tipo", lic.getTipoLicencia());
                respuesta.put("nueva", false);
                return ResponseEntity.ok(respuesta);
            }

            // Buscar licencia existente por email del usuario (creada desde la web)
            String emailUsuario = request.get("email");
            if (emailUsuario != null && !emailUsuario.isBlank()) {
                java.util.Optional<Usuario> usuarioWeb = usuarioRepository.findByEmail(emailUsuario);
                if (usuarioWeb.isPresent()) {
                    java.util.Optional<Licencia> licenciaWeb = licenciaRepository
                            .findByUsuarioIdAndAplicacionId(usuarioWeb.get().getId(), aplicacion.getId());
                    if (licenciaWeb.isPresent() && licenciaWeb.get().getDeviceUuid() == null) {
                        Licencia lic = licenciaWeb.get();
                        lic.setDeviceUuid(deviceUuid);
                        licenciaRepository.save(lic);
                        Map<String, Object> respuesta = new HashMap<>();
                        respuesta.put("codigo", lic.getCodigo());
                        respuesta.put("tipo", lic.getTipoLicencia());
                        respuesta.put("nueva", false);
                        return ResponseEntity.ok(respuesta);
                    }
                }
            }

            // Usar usuario registrado si proporcionó email, sino crear anónimo
            Usuario usuario;
            if (emailUsuario != null && !emailUsuario.isBlank()) {
                usuario = usuarioRepository.findByEmail(emailUsuario).orElseGet(() -> {
                    String emailAnonimo = "device_" + deviceUuid.substring(0, 8) + "@automapro.local";
                    return usuarioRepository.findByEmail(emailAnonimo).orElseGet(() -> {
                        Usuario u = new Usuario();
                        u.setNombre("Usuario " + deviceUuid.substring(0, 8));
                        u.setEmail(emailAnonimo);
                        u.setPassword("$2a$10$disabled");
                        u.setRol("ROLE_CLIENTE");
                        u.setActivo(true);
                        return usuarioRepository.save(u);
                    });
                });
            } else {
                String emailAnonimo = "device_" + deviceUuid.substring(0, 8) + "@automapro.local";
                usuario = usuarioRepository.findByEmail(emailAnonimo).orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setNombre("Usuario " + deviceUuid.substring(0, 8));
                    u.setEmail(emailAnonimo);
                    u.setPassword("$2a$10$disabled");
                    u.setRol("ROLE_CLIENTE");
                    u.setActivo(true);
                    return usuarioRepository.save(u);
                });
            }

            // Crear licencia TRIAL
            String codigo = "LIC-" + java.util.UUID.randomUUID().toString().toUpperCase().substring(0, 8);
            Licencia licencia = new Licencia();
            licencia.setUsuario(usuario);
            licencia.setAplicacion(aplicacion);
            licencia.setCodigo(codigo);
            licencia.setTipoLicencia("TRIAL");
            licencia.setDiasTrial(aplicacion.getDiasTrial() != null ? aplicacion.getDiasTrial() : 30);
            licencia.setActivo(true);
            licencia.setDeviceUuid(deviceUuid);
            licenciaRepository.save(licencia);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("codigo", codigo);
            respuesta.put("tipo", "TRIAL");
            respuesta.put("nueva", true);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Verificar si un email está registrado y obtener estado de licencia
     * GET /api/public/verificar-email?email=X
     */
    @GetMapping("/verificar-email")
    public ResponseEntity<?> verificarEmail(@RequestParam String email,
                                             @RequestParam(required = false) String nombreApp) {
        Map<String, Object> respuesta = new HashMap<>();

        java.util.Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (!usuarioOpt.isPresent()) {
            respuesta.put("existe", false);
            return ResponseEntity.ok(respuesta);
        }

        respuesta.put("existe", true);
        Usuario usuario = usuarioOpt.get();

        // Buscar licencias del usuario
        java.util.List<com.automapro.backend.entity.Licencia> licencias =
            licenciaRepository.findByUsuarioId(usuario.getId());

        if (licencias.isEmpty()) {
            respuesta.put("tipoLicencia", "NINGUNA");
            return ResponseEntity.ok(respuesta);
        }

        // Filtrar por app si se especifica nombreApp
        com.automapro.backend.entity.Licencia licencia = null;
        if (nombreApp != null && !nombreApp.isEmpty()) {
            licencia = licencias.stream()
                .filter(l -> l.getAplicacion() != null &&
                             nombreApp.equalsIgnoreCase(l.getAplicacion().getNombre()))
                .findFirst()
                .orElse(null);
        } else {
            licencia = licencias.get(0);
        }

        if (licencia == null) {
            respuesta.put("tipoLicencia", "NINGUNA");
            return ResponseEntity.ok(respuesta);
        }

        respuesta.put("tipoLicencia", licencia.getTipoLicencia());
        respuesta.put("codigo", licencia.getCodigo());

        if ("TRIAL".equals(licencia.getTipoLicencia()) && licencia.getFechaExpiracion() != null) {
            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), licencia.getFechaExpiracion());
            respuesta.put("diasRestantes", Math.max(0, diasRestantes));
            respuesta.put("expirado", diasRestantes <= 0);
        }

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Verificar si un código pertenece a un email específico
     * GET /api/public/verificar-licencia-email?email=X&codigo=Y
     */
    @GetMapping("/verificar-licencia-email")
    public ResponseEntity<?> verificarLicenciaEmail(
            @RequestParam String email,
            @RequestParam String codigo) {
        Map<String, Object> respuesta = new HashMap<>();

        java.util.Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (!usuarioOpt.isPresent()) {
            respuesta.put("valida", false);
            respuesta.put("mensaje", "Email no registrado");
            return ResponseEntity.ok(respuesta);
        }

        java.util.Optional<com.automapro.backend.entity.Licencia> licenciaOpt = 
            licenciaRepository.findByCodigo(codigo);

        if (!licenciaOpt.isPresent()) {
            respuesta.put("valida", false);
            respuesta.put("mensaje", "Código no encontrado");
            return ResponseEntity.ok(respuesta);
        }

        com.automapro.backend.entity.Licencia licencia = licenciaOpt.get();
        if (!licencia.getUsuario().getId().equals(usuarioOpt.get().getId())) {
            respuesta.put("valida", false);
            respuesta.put("mensaje", "Este código no pertenece a tu cuenta");
            return ResponseEntity.ok(respuesta);
        }

        respuesta.put("valida", true);
        respuesta.put("tipo", licencia.getTipoLicencia());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Recuperar licencia FULL por email (para reinstalaciones)
     * GET /api/public/licencia-por-email?email=X&app=MensajesBiblicos
     */
    @GetMapping("/licencia-por-email")
    public ResponseEntity<?> licenciaPorEmail(
            @RequestParam String email,
            @RequestParam String app) {
        try {
            Aplicacion aplicacion = aplicacionRepository
                    .findByNombreContainingIgnoreCase(app)
                    .stream().filter(Aplicacion::getActivo).findFirst()
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            java.util.Optional<Licencia> licencia = licenciaRepository
                    .findByEmailUsuarioAndAplicacionIdAndTipoLicencia(email, aplicacion.getId(), "FULL");

            if (licencia.isEmpty()) {
                // Buscar por email del usuario registrado
                java.util.Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
                if (usuario.isPresent()) {
                    java.util.Optional<Licencia> lic = licenciaRepository
                            .findByUsuarioIdAndAplicacionId(usuario.get().getId(), aplicacion.getId());
                    if (lic.isPresent() && "FULL".equals(lic.get().getTipoLicencia())) {
                        Map<String, Object> resp = new HashMap<>();
                        resp.put("codigo", lic.get().getCodigo());
                        resp.put("tipo", lic.get().getTipoLicencia());
                        return ResponseEntity.ok(resp);
                    }
                }
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("codigo", licencia.get().getCodigo());
            respuesta.put("tipo", licencia.get().getTipoLicencia());
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Generar licencia TRIAL automáticamente (requiere autenticación)
     * POST /api/public/generar-licencia-trial/{aplicacionId}
     */
    @PostMapping("/generar-licencia-trial/{aplicacionId}")
    public ResponseEntity<?> generarLicenciaTrial(@PathVariable Long aplicacionId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

            if (licenciaRepository.existsByUsuarioIdAndAplicacionId(usuario.getId(), aplicacionId)) {
                return ResponseEntity.badRequest().body("Ya tienes una licencia para esta aplicación");
            }

            LicenciaDTO licenciaDTO = new LicenciaDTO();
            licenciaDTO.setUsuarioId(usuario.getId());
            licenciaDTO.setAplicacionId(aplicacionId);
            licenciaDTO.setTipoLicencia("TRIAL");
            licenciaDTO.setDiasTrial(aplicacion.getDiasTrial() != null ? aplicacion.getDiasTrial() : 30);
            licenciaDTO.setActivo(true);

            LicenciaDTO licenciaCreada = licenciaService.crear(licenciaDTO);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Licencia TRIAL generada exitosamente");
            respuesta.put("licencia", licenciaCreada);
            respuesta.put("aplicacion", aplicacion.getNombre());
            respuesta.put("diasTrial", licenciaCreada.getDiasTrial());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar licencia: " + e.getMessage());
        }
    }

    /**
     * Verificar estado de una licencia via GET (usado por las aplicaciones)
     * GET /api/public/verificar-licencia/{codigo}
     */
    @GetMapping("/verificar-licencia/{codigo}")
    public ResponseEntity<?> verificarLicencia(@PathVariable String codigo) {
        try {
            Licencia licencia = licenciaRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new RuntimeException("Licencia no encontrada"));

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("valida", true);
            respuesta.put("tipo", licencia.getTipoLicencia());
            respuesta.put("activa", licencia.getActivo());
            respuesta.put("aplicacion", licencia.getAplicacion().getNombre());
            respuesta.put("version", licencia.getAplicacion().getVersion());

            if ("TRIAL".equals(licencia.getTipoLicencia())) {
                if (licencia.getFechaInicioUso() == null) {
                    licencia.setFechaInicioUso(LocalDate.now());
                    if (licencia.getDiasTrial() != null && licencia.getDiasTrial() > 0) {
                        licencia.setFechaExpiracion(LocalDate.now().plusDays(licencia.getDiasTrial()));
                    }
                    licenciaRepository.save(licencia);
                }

                if (licencia.getFechaExpiracion() != null) {
                    long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), licencia.getFechaExpiracion());
                    respuesta.put("diasRestantes", diasRestantes > 0 ? diasRestantes : 0);
                    respuesta.put("expirado", diasRestantes <= 0);
                    respuesta.put("fechaExpiracion", licencia.getFechaExpiracion());
                } else {
                    respuesta.put("diasRestantes", licencia.getDiasTrial());
                    respuesta.put("expirado", false);
                }
            } else if ("FULL".equals(licencia.getTipoLicencia())) {
                respuesta.put("permanente", true);
                respuesta.put("expirado", false);
            }

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valida", false);
            error.put("mensaje", "Licencia no válida o no encontrada");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Verificar estado de una licencia via POST (aplicaciones desktop)
     * POST /api/public/verificar-licencia
     */
    @PostMapping("/verificar-licencia")
    public ResponseEntity<?> verificarLicenciaPost(@RequestBody Map<String, String> body) {
        String codigo = body.getOrDefault("codigo", body.get("codigoLicencia"));
        if (codigo == null || codigo.isBlank()) {
            Map<String, Object> error = new HashMap<>();
            error.put("valida", false);
            error.put("mensaje", "Código no proporcionado");
            return ResponseEntity.badRequest().body(error);
        }
        return verificarLicencia(codigo);
    }
}